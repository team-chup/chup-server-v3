package team.themoment.thup.global.storage

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
class LocalFileStorageService(
    private val fileStorageProperties: FileStorageProperties,
) : FileStorageService {
    override fun store(
        file: MultipartFile,
        directory: String,
    ): StoredFile {
        val originalFileName = StringUtils.cleanPath(file.originalFilename ?: "file")
        val extension = StringUtils.getFilenameExtension(originalFileName)
        val storedFileName = UUID.randomUUID().toString() + (if (extension == null) "" else ".$extension")
        val storageKey = "$directory/$storedFileName"

        try {
            val target = resolve(storageKey)
            Files.createDirectories(target.parent)
            file.transferTo(target)
        } catch (e: IOException) {
            throw ExpectedException("파일 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return StoredFile(storageKey, originalFileName, file.size)
    }

    override fun loadAsResource(storageKey: String): Resource {
        val resource = FileSystemResource(resolve(storageKey))
        if (!resource.exists() || !resource.isReadable) {
            throw ExpectedException("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return resource
    }

    override fun delete(storageKey: String) {
        try {
            Files.deleteIfExists(resolve(storageKey))
        } catch (ignored: IOException) {
            // 파일 삭제 실패는 치명적이지 않으므로 무시
        }
    }

    private fun resolve(storageKey: String): Path {
        val baseDir = Path.of(fileStorageProperties.baseDir).toAbsolutePath().normalize()
        val target = baseDir.resolve(storageKey).normalize()
        if (!target.startsWith(baseDir)) {
            throw ExpectedException("잘못된 파일 경로입니다.", HttpStatus.BAD_REQUEST)
        }
        return target
    }
}
