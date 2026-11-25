package team.themoment.thup.domain.job.util

import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.entity.constant.AttachmentFileType

object AttachmentFileTypeResolver {
    fun resolve(file: MultipartFile): AttachmentFileType {
        val contentType = file.contentType
        val fileName = file.originalFilename?.lowercase() ?: ""

        if (fileName.endsWith(".pdf") || contentType == "application/pdf") {
            return AttachmentFileType.PDF
        }
        if (fileName.endsWith(".hwpx")) {
            return AttachmentFileType.HWPX
        }
        if (fileName.endsWith(".hwp")) {
            return AttachmentFileType.HWP
        }
        if (contentType != null && contentType.startsWith("image/")) {
            return AttachmentFileType.IMAGE
        }

        throw ExpectedException("지원하지 않는 첨부파일 형식입니다. (PDF/HWP/HWPX/이미지만 가능)", HttpStatus.BAD_REQUEST)
    }
}
