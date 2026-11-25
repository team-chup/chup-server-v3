package team.themoment.thup.domain.user.service

import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.user.dto.ResumeResponse
import team.themoment.thup.domain.user.entity.ResumeJpaEntity
import team.themoment.thup.domain.user.repository.ResumeRepository
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.security.userId
import team.themoment.thup.global.storage.FileStorageService

@Service
class RegisterResumeService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional
    fun execute(
        user: OAuth2User,
        file: MultipartFile?,
    ): ResumeResponse {
        val userId = user.userId
        val validFile = validate(file)

        // 동시 업로드 요청이 카운트 체크를 동시에 통과해 3개 제한을 넘기지 않도록,
        // 유저 row에 락을 건 뒤 그 안에서 카운트를 확인한다.
        val foundUser =
            userRepository.findByIdForUpdate(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (resumeRepository.countByUserId(userId) >= MAX_RESUMES) {
            throw ExpectedException("이력서는 최대 ${MAX_RESUMES}개까지 등록할 수 있습니다.", HttpStatus.CONFLICT)
        }

        val stored = fileStorageService.store(validFile, "resumes/$userId")

        val saved =
            resumeRepository.save(
                ResumeJpaEntity(
                    user = foundUser,
                    fileName = stored.originalFileName,
                    fileUrl = stored.storageKey,
                    fileSize = stored.size.toInt(),
                ),
            )

        return ResumeResponse.from(saved)
    }

    private fun validate(file: MultipartFile?): MultipartFile {
        if (file == null || file.isEmpty) {
            throw ExpectedException("이력서 파일이 필요합니다.", HttpStatus.BAD_REQUEST)
        }
        val isPdfContentType = file.contentType == "application/pdf"
        val isPdfExtension = file.originalFilename?.lowercase()?.endsWith(".pdf") == true
        if (!isPdfContentType && !isPdfExtension) {
            throw ExpectedException("PDF 파일만 업로드할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }
        if (file.size > MAX_RESUME_SIZE_BYTES) {
            throw ExpectedException("파일 용량이 너무 큽니다.", HttpStatus.PAYLOAD_TOO_LARGE)
        }
        return file
    }

    companion object {
        private const val MAX_RESUMES = 3
        private const val MAX_RESUME_SIZE_BYTES = 20L * 1024 * 1024
    }
}
