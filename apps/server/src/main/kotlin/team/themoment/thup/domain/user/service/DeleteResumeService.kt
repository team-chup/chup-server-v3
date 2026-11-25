package team.themoment.thup.domain.user.service

import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.user.repository.ResumeRepository
import team.themoment.thup.global.security.userId

@Service
class DeleteResumeService(
    private val resumeRepository: ResumeRepository,
) {
    @Transactional
    fun execute(
        user: OAuth2User,
        resumeId: Long,
    ) {
        val resume =
            resumeRepository.findByIdAndUserId(resumeId, user.userId)
                ?: throw ExpectedException("등록된 이력서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        resumeRepository.delete(resume)
    }
}
