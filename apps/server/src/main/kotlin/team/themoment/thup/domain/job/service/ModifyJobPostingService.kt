package team.themoment.thup.domain.job.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.job.dto.JobPostingDetailResponse
import team.themoment.thup.domain.job.entity.AttachmentJpaEntity
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.domain.job.util.AttachmentFileTypeResolver
import team.themoment.thup.global.storage.FileStorageService
import java.time.LocalDate

@Service
class ModifyJobPostingService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val attachmentRepository: AttachmentRepository,
    private val applicationRepository: ApplicationRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional
    fun execute(
        jobId: Long,
        companyName: String,
        description: String,
        employmentType: EmploymentType,
        recruitStart: LocalDate,
        recruitEnd: LocalDate,
        positionNames: List<String>?,
        retainedAttachmentIds: List<Long>?,
        attachments: List<MultipartFile>?,
    ): JobPostingDetailResponse {
        val jobPosting =
            jobPostingRepository.findByIdOrNull(jobId)
                ?: throw ExpectedException("공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        jobPosting.update(companyName, description, employmentType, recruitStart, recruitEnd)

        if (positionNames != null) {
            updatePositions(jobPosting, positionNames)
        }

        if (retainedAttachmentIds != null || !attachments.isNullOrEmpty()) {
            updateAttachments(jobPosting, retainedAttachmentIds, attachments)
        }

        val currentPositions = jobPositionRepository.findAllByJobPostingId(jobId)
        val currentAttachments = attachmentRepository.findAllByJobPostingId(jobId)
        return JobPostingDetailResponse.of(jobPosting, currentPositions, currentAttachments)
    }

    private fun updatePositions(
        jobPosting: JobPostingJpaEntity,
        positionNames: List<String>,
    ) {
        val newNames =
            positionNames
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

        val existing = jobPositionRepository.findAllByJobPostingId(jobPosting.id!!)
        val existingNames = existing.map { it.name }.toSet()

        val blockedByApplicants = mutableListOf<String>()
        for (position in existing) {
            if (position.name in newNames) {
                continue
            }
            if (applicationRepository.existsByJobPositionId(position.id!!)) {
                blockedByApplicants.add(position.name)
                continue
            }
            jobPositionRepository.delete(position)
        }

        if (blockedByApplicants.isNotEmpty()) {
            throw ExpectedException(
                "지원자가 있는 포지션은 삭제할 수 없습니다: " + blockedByApplicants.joinToString(", "),
                HttpStatus.CONFLICT,
            )
        }

        newNames
            .filter { it !in existingNames }
            .forEach { name -> jobPositionRepository.save(JobPositionJpaEntity(jobPosting = jobPosting, name = name)) }
    }

    private fun updateAttachments(
        jobPosting: JobPostingJpaEntity,
        retainedAttachmentIds: List<Long>?,
        attachments: List<MultipartFile>?,
    ) {
        val newFiles = attachments ?: emptyList()
        val existing = attachmentRepository.findAllByJobPostingId(jobPosting.id!!)

        // retainedAttachmentIds를 안 보내면(null) "새 파일만 추가, 기존은 그대로 유지"가 안전한 기본값이다.
        // 기존 전부를 지우고 싶으면 retainedAttachmentIds=[]를 명시적으로 보내야 한다.
        val retainedIds = retainedAttachmentIds?.toSet() ?: existing.map { it.id }.toSet()

        val toDelete = existing.filter { it.id !in retainedIds }
        val retainedCount = existing.size - toDelete.size

        if (retainedCount + newFiles.size > MAX_ATTACHMENTS) {
            throw ExpectedException("첨부파일은 최대 ${MAX_ATTACHMENTS}개까지 등록할 수 있습니다.", HttpStatus.PAYLOAD_TOO_LARGE)
        }

        val fileTypes =
            newFiles.map { file ->
                if (file.size > MAX_ATTACHMENT_SIZE_BYTES) {
                    throw ExpectedException("첨부파일 용량이 너무 큽니다.", HttpStatus.PAYLOAD_TOO_LARGE)
                }
                AttachmentFileTypeResolver.resolve(file)
            }

        attachmentRepository.deleteAll(toDelete)
        toDelete.forEach { fileStorageService.delete(it.fileUrl) }

        newFiles.forEachIndexed { index, file ->
            val stored = fileStorageService.store(file, "job-attachments/${jobPosting.id}")

            attachmentRepository.save(
                AttachmentJpaEntity(
                    jobPosting = jobPosting,
                    fileName = stored.originalFileName,
                    fileUrl = stored.storageKey,
                    fileType = fileTypes[index],
                    fileSize = stored.size.toInt(),
                ),
            )
        }
    }

    companion object {
        private const val MAX_ATTACHMENTS = 5
        private const val MAX_ATTACHMENT_SIZE_BYTES = 20L * 1024 * 1024
    }
}
