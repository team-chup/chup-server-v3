package team.themoment.thup.global.storage

data class StoredFile(
    val storageKey: String,
    val originalFileName: String,
    val size: Long,
)
