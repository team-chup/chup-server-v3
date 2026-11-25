package team.themoment.thup.global.storage

import org.springframework.core.io.Resource

data class FileDownload(
    val resource: Resource,
    val fileName: String,
)
