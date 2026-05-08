package io.github.opensmsrelay.forwarding

import io.github.opensmsrelay.core.common.Constants
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.core.common.toReadableDateTime
import io.github.opensmsrelay.domain.model.EmailSettings
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SecurityType
import io.github.opensmsrelay.domain.model.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Singleton
class EmailForwarder @Inject constructor() {

    suspend fun send(
        smsMessage: SmsMessage,
        rule: Rule,
        settings: EmailSettings,
        maskedBody: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val props = buildProperties(settings)
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(settings.username, settings.password)
            })
            session.debug = false

            val subject = Constants.EMAIL_SUBJECT_TEMPLATE
                .replace("{sender}", smsMessage.sender)
                .replace("{time}", smsMessage.receivedAt.toReadableDateTime())
            val body = buildEmailBody(smsMessage, rule, maskedBody)

            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(settings.fromEmail))
                setRecipients(
                    Message.RecipientType.TO,
                    settings.recipients.map { InternetAddress(it) }.toTypedArray()
                )
                setSubject(subject, "UTF-8")
                setText(body, "UTF-8")
            }

            Transport.send(mimeMessage)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Error(it.message ?: "Email send failed", it) }
        )
    }

    private fun buildProperties(settings: EmailSettings): Properties = Properties().apply {
        put("mail.smtp.host", settings.host)
        put("mail.smtp.port", settings.port.toString())
        put("mail.smtp.auth", "true")
        put("mail.smtp.connectiontimeout", "15000")
        put("mail.smtp.timeout", "15000")
        put("mail.smtp.writetimeout", "15000")

        when (settings.securityType) {
            SecurityType.SSL_TLS -> {
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.ssl.checkserveridentity", "true")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
            }
            SecurityType.STARTTLS -> {
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
            }
            SecurityType.NONE -> {
                // No TLS; not recommended
            }
        }
    }

    private fun buildEmailBody(
        smsMessage: SmsMessage,
        rule: Rule,
        maskedBody: String
    ): String = """
        Open SMS Relay

        Rule: ${rule.name}
        From: ${smsMessage.sender}
        Received: ${smsMessage.receivedAt.toReadableDateTime()}

        Message:
        $maskedBody
    """.trimIndent()
}
