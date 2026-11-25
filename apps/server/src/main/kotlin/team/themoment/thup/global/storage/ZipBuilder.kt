package team.themoment.thup.global.storage

import org.springframework.http.HttpStatus
import team.themoment.sdk.exception.ExpectedException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipBuilder {
    data class Entry(
        val path: String,
        val storageKey: String,
    )

    fun build(
        entries: List<Entry>,
        fileStorageService: FileStorageService,
    ): ByteArray {
        val buffer = ByteArrayOutputStream()

        try {
            ZipOutputStream(buffer).use { zipOutputStream ->
                for (entry in entries) {
                    zipOutputStream.putNextEntry(ZipEntry(entry.path))
                    fileStorageService.loadAsResource(entry.storageKey).inputStream.use { inputStream ->
                        inputStream.transferTo(zipOutputStream)
                    }
                    zipOutputStream.closeEntry()
                }
            }
        } catch (e: IOException) {
            throw ExpectedException("ZIP 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return buffer.toByteArray()
    }

    fun uniqueName(
        fileName: String,
        usedNames: MutableMap<String, Int>,
    ): String {
        val count = usedNames.merge(fileName, 1, Int::plus)!!
        if (count == 1) {
            return fileName
        }
        val dotIndex = fileName.lastIndexOf('.')
        val base = if (dotIndex == -1) fileName else fileName.substring(0, dotIndex)
        val extension = if (dotIndex == -1) "" else fileName.substring(dotIndex)
        return "$base(${count - 1})$extension"
    }
}
