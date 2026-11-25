package team.themoment.thup.domain.job.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.discord.JobPostingDiscordNotifier
import team.themoment.thup.domain.job.discord.JobPostingNotification
import team.themoment.thup.domain.job.dto.JobPostingDetailResponse
import team.themoment.thup.domain.job.entity.AttachmentJpaEntity
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.domain.job.util.AttachmentFileTypeResolver
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.security.userId
import team.themoment.thup.global.storage.FileStorageService
import java.time.LocalDate

@Service
class CreateJobPostingService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val attachmentRepository: AttachmentRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService,
    private val jobPostingDiscordNotifier: JobPostingDiscordNotifier,
) {
    @Transactional
    fun execute(
        admin: OAuth2User,
        companyName: String,
        description: String,
        employmentType: EmploymentType,
        recruitStart: LocalDate,
        recruitEnd: LocalDate,
        positionNames: List<String>,
        attachments: List<MultipartFile>?,
    ): JobPostingDetailResponse {
        validateAttachments(attachments)

        val createdBy =
            userRepository.findByIdOrNull(admin.userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val saved =
            jobPostingRepository.save(
                JobPostingJpaEntity(
                    createdBy = createdBy,
                    companyName = companyName,
                    description = description,
                    employmentType = employmentType,
                    recruitStart = recruitStart,
                    recruitEnd = recruitEnd,
                ),
            )

        val savedPositions =
            positionNames.map { name ->
                jobPositionRepository.save(JobPositionJpaEntity(jobPosting = saved, name = name))
            }

        val savedAttachments = saveAttachments(saved, attachments)

        jobPostingDiscordNotifier.notifyCreated(
            JobPostingNotification(
                companyName = saved.companyName,
                description = saved.description,
                employmentType = saved.employmentType,
                positionNames = savedPositions.map { it.name },
                recruitStart = saved.recruitStart,
                recruitEnd = saved.recruitEnd,
                createdByName = createdBy.name,
            ),
        )

        return JobPostingDetailResponse.of(saved, savedPositions, savedAttachments)
    }

    private fun validateAttachments(attachments: List<MultipartFile>?) {
        if (attachments.isNullOrEmpty()) {
            return
        }
        if (attachments.size > MAX_ATTACHMENTS) {
            throw ExpectedException("첨부파일은 최대 ${MAX_ATTACHMENTS}개까지 등록할 수 있습니다.", HttpStatus.PAYLOAD_TOO_LARGE)
        }
        for (file in attachments) {
            if (file.size > MAX_ATTACHMENT_SIZE_BYTES) {
                throw ExpectedException("첨부파일 용량이 너무 큽니다.", HttpStatus.PAYLOAD_TOO_LARGE)
            }
        }
    }

    private fun saveAttachments(
        jobPosting: JobPostingJpaEntity,
        attachments: List<MultipartFile>?,
    ): List<AttachmentJpaEntity> {
        if (attachments == null) {
            return emptyList()
        }
        return attachments.map { file ->
            val fileType = AttachmentFileTypeResolver.resolve(file)
            val stored = fileStorageService.store(file, "job-attachments/${jobPosting.id}")

            attachmentRepository.save(
                AttachmentJpaEntity(
                    jobPosting = jobPosting,
                    fileName = stored.originalFileName,
                    fileUrl = stored.storageKey,
                    fileType = fileType,
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
