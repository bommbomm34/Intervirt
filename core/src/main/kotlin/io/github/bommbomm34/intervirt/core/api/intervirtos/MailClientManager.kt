package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.store.IntervirtOSStore
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionDetails
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety
import io.github.bommbomm34.intervirt.core.data.toMail
import io.github.bommbomm34.intervirt.core.parseAddress
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.util.AsyncCloseable
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MailClientManager(
    osClient: IntervirtOSClient
) : AsyncCloseable {
    private val store = osClient.getClient(this).store
    private val logger = KotlinLogging.logger { }
    private var smtpSession: Session? = null
    private var imapStore: Store? = null
    val isInitialized: Boolean
        get() = smtpSession != null && imapStore != null

    suspend fun init(
        mailConnectionDetails: MailConnectionDetails,
        proxy: Address
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            fun Properties.putDefaultProperties(
                ref: String,
                addr: Address,
                safety: MailConnectionSafety
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
            logger.debug { "Successfully initialized both SMTP and IMAP" }
        }
    }

    suspend fun sendMail(mail: Mail): Result<Unit> {
        val session = smtpSession
        check(session != null) { "SMTP session isn't successfully initialized" }
        return withContext(Dispatchers.IO) {
            runCatching {
                Transport.send(mail.getMimeMessage(session))
            }
        }
    }

    suspend fun getMails(): Result<List<Mail>> {
        val store = imapStore
        check(store != null) { "IMAP session isn't successfully initialized" }
        return withContext(Dispatchers.IO) {
            runCatching {
                store.useInbox {
                    messages.mapIndexedNotNull { i, msg ->
                        val mail = msg.toMail(i)
                        mail.fold(
                            onSuccess = {
                                logger.debug { "Received mail: $it" }
                                it
                            },
                            onFailure = {
                                logger.error(it) { "Invalid email: $msg" }
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteMail(mail: Mail): Result<Unit> {
        val store = imapStore
        check(store != null) { "IMAP session isn't successfully initialized" }
        require(mail.index != null) { "Mail doesn't include an index" }
        return withContext(Dispatchers.IO) {
            runCatching {
                store.useInbox {
                    messages[mail.index].setFlag(Flags.Flag.DELETED, true)
                }
            }
        }
    }

    suspend fun saveCredentials(details: MailConnectionDetails) = runSuspendingCatching {
        store.set(IntervirtOSStore.Accessor.MAIL_USERNAME, details.username).getOrThrow()
        store.set(IntervirtOSStore.Accessor.MAIL_PASSWORD, details.password).getOrThrow()
        store.set(IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS, details.smtpAddress.toString()).getOrThrow()
        store.set(IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS, details.imapAddress.toString()).getOrThrow()
    }

    fun loadCredentials() = MailConnectionDetails(
        smtpAddress = store[IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS].parseAddress(),
        imapAddress = store[IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS].parseAddress(),
        username = store[IntervirtOSStore.Accessor.MAIL_USERNAME],
        password = store[IntervirtOSStore.Accessor.MAIL_PASSWORD]
    )

    suspend fun clearCredentials(): Result<Unit> = runSuspendingCatching {
        store.delete(IntervirtOSStore.Accessor.MAIL_USERNAME).getOrThrow()
        store.delete(IntervirtOSStore.Accessor.MAIL_PASSWORD).getOrThrow()
        store.delete(IntervirtOSStore.Accessor.SMTP_SERVER_ADDRESS).getOrThrow()
        store.delete(IntervirtOSStore.Accessor.IMAP_SERVER_ADDRESS).getOrThrow()
    }

    private suspend inline fun <T> Store.useInbox(
        mode: Int = Folder.READ_ONLY,
        crossinline block: Folder.() -> T
    ): T = withContext(Dispatchers.IO) {
        val inbox = getFolder(URLName("INBOX"))
        inbox.open(mode)
        val res = inbox.block()
        inbox.close()
        return@withContext res
    }

    private fun getAuthenticator(
        username: String,
        password: String
    ): Authenticator = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO){
        runCatching {
            logger.debug { "Closing SMTP session" }
            smtpSession?.transport?.close()
            logger.debug { "Closing IMAP session" }
            imapStore?.close()
            Unit
        }
    }
}