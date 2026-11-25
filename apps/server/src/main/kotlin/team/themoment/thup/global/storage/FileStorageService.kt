package team.themoment.thup.global.storage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun store(
        file: MultipartFile,
        directory: String,
    ): StoredFile

    fun loadAsResource(storageKey: String): Resource

    fun delete(storageKey: String)
}
