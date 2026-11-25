package team.themoment.thup.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dev-tools")
data class DevToolsProperties(
    val enabled: Boolean = false,
)
