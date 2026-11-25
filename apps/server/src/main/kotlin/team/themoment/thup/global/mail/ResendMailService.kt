package team.themoment.thup.global.mail

import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.model.CreateEmailOptions
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ResendMailService(
    private val resend: Resend,
    private val mailProperties: MailProperties,
) : MailService {
    @Async
    override fun send(
        to: String,
        subject: String,
        htmlBody: String,
    ) {
        if (mailProperties.apiKey.isNullOrBlank()) {
            log.warn("Skipped email because RESEND_API_KEY is not configured. to={}, subject={}", to, subject)
            return
        }

        val params =
            CreateEmailOptions
                .builder()
                .from(mailProperties.fromAddress)
                .to(to)
                .subject(subject)
                .html(htmlBody)
                .build()

        try {
            resend.emails().send(params)
        } catch (e: ResendException) {
            log.warn("Failed to send email. to={}, subject={}, error={}", to, subject, e.message)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResendMailService::class.java)
    }
}
