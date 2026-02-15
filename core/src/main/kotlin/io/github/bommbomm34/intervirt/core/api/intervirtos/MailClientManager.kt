package io.github.bommbomm34.intervirt.core.api.intervirtos

import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.Mail
import io.github.bommbomm34.intervirt.core.data.toMail
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.Authenticator
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.Transport
import jakarta.mail.URLName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MailClientManager(
    bundle: ContainerClientBundle
) : AutoCloseable {

    private val logger = KotlinLogging.logger {  }
    private var smtpSession: Session? = null
    private var imapStore: Store? = null

    suspend fun init(
        host: Address,
        proxy: Address,
        username: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Init SMTP
            val authenticator = getAuthenticator(username, password)
            val smtpProperties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", host.host)
                put("mail.smtp.port", host.port.toString())
                put("mail.smtp.socks.host", proxy.host)
                put("mail.smtp.socks.port", proxy.port.toString())
            }
            smtpSession = Session.getInstance(smtpProperties, authenticator)
            // Init IMAP
            val imapProperties = Properties().apply {
                put("mail.imap.auth", "true")
                put("mail.imap.host", host.host)
                put("mail.imap.port", host.port.toString())
                put("mail.imap.socks.host", proxy.host)
                put("mail.imap.socks.port", proxy.port.toString())
            }
            val imapSession = Session.getInstance(imapProperties)
            val store = imapSession.getStore("imap")
            store.connect(username, password)
            imapStore = store
        }
    }

    suspend fun sendMail(mail: Mail): Result<Unit> {
        val session = smtpSession
        require(session != null) { "SMTP session isn't successfully initialized" }
        return withContext(Dispatchers.IO) {
            runCatching {
                Transport.send(mail.getMimeMessage(session))
            }
        }
    }

    suspend fun getMails(): Result<List<Mail>> {
        val store = imapStore
        require(store != null) { "IMAP session isn't successfully initialized" }
        return withContext(Dispatchers.IO){
            runCatching {
                store.useInbox {
                    messages.mapIndexedNotNull { i, msg ->
                        val mail = msg.toMail(i)
                        mail.fold(
                            onSuccess = { it },
                            onFailure = {
                                logger.error(it){ "Invalid email: $msg" }
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
        require(store != null) { "IMAP session isn't successfully initialized" }
        require(mail.index != null) { "Mail doesn't include an index" }
        return withContext(Dispatchers.IO){
            runCatching {
                store.useInbox {
                    messages[mail.index].setFlag(Flags.Flag.DELETED, true)
                }
            }
        }
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

    override fun close() {
        smtpSession?.transport?.close()
    }
}