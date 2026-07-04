/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api.intervirtos

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSStore
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure

import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.MailUser
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety
import io.github.bommbomm34.intervirt.core.data.toMail
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.core.util.ext.parseMailAddress
import io.github.bommbomm34.intervirt.core.util.ext.withCatchingContext
import io.github.bommbomm34.intervirt.secret.SecretService

import jakarta.mail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

// TODO: Improve error handling
class MailClientManager(
    osClient: IntervirtOSClient,
    appEnv: AppEnv,
    private val secretService: SecretService,
) : AsyncCloseable {
    private val client = osClient.getClient(this)
    private val store = client.store
    private val mailPasswordKey = "MAIL_PASSWORD_${client.computer.id}"
    private val logger = appEnv.getLogger(MailClientManager::class)
    private var smtpSession: Session? = null
    private var imapStore: Store? = null
    var mailUser: MailUser? = null

    context(_: Raise<Failure>)
    suspend fun init(
        mailConnectionDetails: MailConnectionDetails,
        proxy: Address,
    ) = withCatchingContext(Dispatchers.IO) {
        fun Properties.putDefaultProperties(
            ref: String,
            addr: Address,
            safety: MailConnectionSafety,
        ) {
            when (safety) {
                MailConnectionSafety.STARTTLS -> {
                    put("mail.$ref.starttls.enable", true)
                    put("mail.$ref.starttls.required", true)
                }

                MailConnectionSafety.SECURE -> put("mail.$ref.ssl.enable", true)
                else -> {}
            }
            put("mail.$ref.auth", true)
            put("mail.$ref.host", addr.host)
            put("mail.$ref.port", addr.port)
            put("mail.$ref.socks.host", proxy.host)
            put("mail.$ref.socks.port", proxy.port)
        }
        val (smtp, smtpSafety, imap, imapSafety, username, password) = mailConnectionDetails
        // Init SMTP
        logger.debug { "Initializing connection via SMTP with $smtp" }
        val authenticator = getAuthenticator(username, password)
        val smtpProperties = Properties().apply {
            val ref = when (smtpSafety) {
                MailConnectionSafety.SECURE -> "smtps"
                else -> "smtp"
            }
            putDefaultProperties(ref, smtp, smtpSafety)
        }
        smtpSession = Session.getInstance(smtpProperties, authenticator)
        logger.debug { "Initializing connection via IMAP with $imap" }
        val imapRef = when (imapSafety) {
            MailConnectionSafety.SECURE -> "imaps"
            else -> "imap"
        }
        // Init IMAP
        val imapProperties = Properties().apply {

            if (imapSafety == MailConnectionSafety.STARTTLS) {
                put("mail.$imapRef.starttls.enable", true)
                put("mail.$imapRef.starttls.required", true)
            }
            putDefaultProperties(imapRef, imap, imapSafety)
        }
        val imapSession = Session.getInstance(imapProperties)
        val store = imapSession.getStore(imapRef)
        store.connect(username, password)
        imapStore = store
        mailUser = username.parseMailAddress()
        logger.debug { "Successfully initialized both SMTP and IMAP" }
    }

    context(_: Raise<Failure>)
    suspend fun sendMail(mail: Mail) {
        val session = getSmtpSession()
        return withCatchingContext(Dispatchers.IO) {
            logger.debug { "Sending mail $mail" }
            Transport.send(mail.getMessage(session))
        }
    }

    context(_: Raise<Failure>)
    suspend fun getReplyMail(mail: Mail): Mail {
        val imapStore = getImapStore()
        require(mail.index != null) { "Mail doesn't include an index" }
        return withCatchingContext(Dispatchers.IO) {
            logger.debug { "Generating reply mail of $mail" }
            imapStore.useInbox {
                messages[mail.index].reply(false)
                    .toMail(senderOptional = true)
            }
        }
    }

    context(_: Raise<Failure>)
    suspend fun getMails(): List<Mail> {
        val store = getImapStore()
        return withCatchingContext(Dispatchers.IO) {
            store.useInbox {
                messages.mapIndexedNotNull { i, msg ->
                    val mail = msg.toMail(i)
                    mail
                }
            }
        }
    }

    context(_: Raise<Failure>)
    suspend fun deleteMail(mail: Mail) {
        val store = imapStore
        check(store != null) { "IMAP session isn't successfully initialized" }
        require(mail.index != null) { "Mail doesn't include an index" }
        return withCatchingContext(Dispatchers.IO) {
            logger.debug { "Deleting mail '${mail.subject}'" }
            store.useInbox(Folder.READ_WRITE) {
                messages[mail.index].setFlag(Flags.Flag.DELETED, true)
            }
        }
    }

    context(_: Raise<Failure>)
    suspend fun saveCredentials(details: MailConnectionDetails) {
        store.set(IntervirtOSStore.Accessor.MAIL_USERNAME, details.username)
        secretService.setEntry(mailPasswordKey, details.password.encodeToByteArray()).onFailure { raise(Failure.Unexpected(it)) }
        store.set(IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS, details.smtpAddress)
        store.set(IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS, details.imapAddress)
        store.set(IntervirtOSStore.Accessor.SMTP_SAFETY, details.smtpSafety)
        store.set(IntervirtOSStore.Accessor.IMAP_SAFETY, details.imapSafety)
    }

    suspend fun loadCredentials(): MailConnectionDetails {
        return MailConnectionDetails(
            smtpAddress = store[IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS],
            imapAddress = store[IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS],
            username = store[IntervirtOSStore.Accessor.MAIL_USERNAME],
            password = secretService.getEntry(mailPasswordKey).getOrThrow()?.decodeToString() ?: "",
            smtpSafety = store[IntervirtOSStore.Accessor.SMTP_SAFETY],
            imapSafety = store[IntervirtOSStore.Accessor.IMAP_SAFETY],
        )
    }

    context(_: Raise<Failure>)
    suspend fun clearCredentials() {
        store.delete(IntervirtOSStore.Accessor.MAIL_USERNAME)
        secretService.removeEntry(mailPasswordKey).onFailure { raise(Failure.Unexpected(it)) }
        store.delete(IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS)
        store.delete(IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS)
        store.delete(IntervirtOSStore.Accessor.SMTP_SAFETY)
        store.delete(IntervirtOSStore.Accessor.IMAP_SAFETY)
    }

    private suspend inline fun <T> Store.useInbox(
        mode: Int = Folder.READ_ONLY,
        crossinline block: Folder.() -> T,
    ): T = withContext(Dispatchers.IO) {
        val inbox = getFolder(URLName("INBOX"))
        inbox.open(mode)
        val res = inbox.block()
        inbox.close(true)
        return@withContext res
    }

    private fun getAuthenticator(
        username: String,
        password: String,
    ): Authenticator = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    private fun getSmtpSession(): Session {
        val session = smtpSession
        check(session != null) { "SMTP session isn't successfully initialized" }
        return session
    }

    private fun getImapStore(): Store {
        val store = imapStore
        check(store != null) { "IMAP session isn't successfully initialized" }
        return store
    }

    context(_: Raise<Failure>)
    override suspend fun close() = withCatchingContext(Dispatchers.IO) {
        logger.debug { "Closing SMTP session" }
        smtpSession?.transport?.close()
        logger.debug { "Closing IMAP session" }
        imapStore?.close()
        Unit
    }
}
