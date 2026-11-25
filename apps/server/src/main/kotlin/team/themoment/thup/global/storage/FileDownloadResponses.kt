package team.themoment.thup.global.storage

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FileDownloadResponses {
    fun of(download: FileDownload): ResponseEntity<Resource> {
        val encodedFileName = URLEncoder.encode(download.fileName, StandardCharsets.UTF_8).replace("+", "%20")

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFileName")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(download.resource)
    }
}
