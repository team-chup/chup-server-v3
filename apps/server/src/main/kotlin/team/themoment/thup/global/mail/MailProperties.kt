package team.themoment.thup.global.mail

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail")
data class MailProperties(
    val apiKey: String?,
    val fromAddress: String?,
)
