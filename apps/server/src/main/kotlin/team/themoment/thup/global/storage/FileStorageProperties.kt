package team.themoment.thup.global.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
data class FileStorageProperties(
    val baseDir: String,
)
