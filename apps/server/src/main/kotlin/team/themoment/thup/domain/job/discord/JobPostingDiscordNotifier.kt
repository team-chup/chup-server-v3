package team.themoment.thup.domain.job.discord

import org.springframework.stereotype.Component
import team.themoment.thup.global.discord.DiscordProperties
import team.themoment.thup.global.discord.DiscordWebhookClient

@Component
class JobPostingDiscordNotifier(
    private val discordWebhookClient: DiscordWebhookClient,
    private val discordProperties: DiscordProperties,
) {
    fun notifyCreated(notification: JobPostingNotification) {
        discordWebhookClient.send(
            discordProperties.jobWebhookUrl,
            JobPostingDiscordTemplate.build(
                notification,
                discordProperties.envLabel,
                discordProperties.clientJobsUrl,
            ),
        )
    }
}
