package team.themoment.thup.global.discord

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "discord")
data class DiscordProperties(
    val envLabel: String?,
    val jobWebhookUrl: String?,
    val errorWebhookUrl: String?,
    val clientJobsUrl: String?,
    val errorCooldown: Duration,
)
