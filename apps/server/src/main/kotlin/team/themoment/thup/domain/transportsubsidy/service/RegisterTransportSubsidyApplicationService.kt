package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.transportsubsidy.dto.MyTransportSubsidyResponse
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyApplicationJpaEntity
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyEvidenceJpaEntity
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.security.userId
import team.themoment.thup.global.storage.FileStorageService
import java.time.LocalDateTime

@Service
class RegisterTransportSubsidyApplicationService(
    private val applicationRepository: TransportSubsidyApplicationRepository,
    private val evidenceRepository: TransportSubsidyEvidenceRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional
    fun execute(
        user: OAuth2User,
        companyName: String?,
        interviewAt: LocalDateTime?,
        files: List<MultipartFile>?,
    ): MyTransportSubsidyResponse {
        val userId = user.userId

        if (companyName.isNullOrBlank()) {
            throw ExpectedException("면접 회사명을 입력해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (interviewAt == null) {
            throw ExpectedException("면접 일시를 입력해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        val validFiles = validateFiles(files)

        val foundUser =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (foundUser.role != Role.STUDENT) {
            throw ExpectedException("학생만 교통비 지원을 신청할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val application =
            applicationRepository.save(
                TransportSubsidyApplicationJpaEntity(
                    user = foundUser,
                    companyName = companyName,
                    interviewAt = interviewAt,
                ),
            )

        val evidences =
            validFiles.map { file ->
                val stored = fileStorageService.store(file, "transport-subsidies/$userId")
                evidenceRepository.save(
                    TransportSubsidyEvidenceJpaEntity(
                        application = application,
                        fileName = stored.originalFileName,
                        fileUrl = stored.storageKey,
                        fileSize = stored.size.toInt(),
                    ),
                )
            }

        return MyTransportSubsidyResponse.from(application, evidences)
    }

    private fun validateFiles(files: List<MultipartFile>?): List<MultipartFile> {
        if (files.isNullOrEmpty()) {
            throw ExpectedException("증빙 서류를 하나 이상 첨부해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (files.size > MAX_EVIDENCE_FILES) {
            throw ExpectedException("증빙 서류는 최대 ${MAX_EVIDENCE_FILES}개까지 첨부할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }
        for (file in files) {
            if (file.isEmpty) {
                throw ExpectedException("빈 파일은 첨부할 수 없습니다.", HttpStatus.BAD_REQUEST)
            }
            if (file.contentType == null || file.contentType !in ALLOWED_CONTENT_TYPES) {
                throw ExpectedException("이미지(jpg/png) 또는 PDF 파일만 첨부할 수 있습니다.", HttpStatus.BAD_REQUEST)
            }
            if (file.size > MAX_FILE_SIZE_BYTES) {
                throw ExpectedException("파일 용량이 너무 큽니다.", HttpStatus.PAYLOAD_TOO_LARGE)
            }
        }
        return files
    }

    companion object {
        private const val MAX_EVIDENCE_FILES = 5
        private const val MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png", "application/pdf")
    }
}
