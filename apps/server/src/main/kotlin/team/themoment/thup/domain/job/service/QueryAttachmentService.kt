package team.themoment.thup.domain.job.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.global.storage.FileDownload
import team.themoment.thup.global.storage.FileStorageService

@Service
class QueryAttachmentService(
    private val attachmentRepository: AttachmentRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional(readOnly = true)
    fun execute(
        jobId: Long,
        fileId: Long,
    ): FileDownload {
        val attachment =
            attachmentRepository.findByIdAndJobPostingId(fileId, jobId)
                ?: throw ExpectedException("첨부파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        return FileDownload(fileStorageService.loadAsResource(attachment.fileUrl), attachment.fileName)
    }
}
