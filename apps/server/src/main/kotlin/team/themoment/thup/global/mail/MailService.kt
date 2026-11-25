package team.themoment.thup.global.mail

interface MailService {
    fun send(
        to: String,
        subject: String,
        htmlBody: String,
    )
}
