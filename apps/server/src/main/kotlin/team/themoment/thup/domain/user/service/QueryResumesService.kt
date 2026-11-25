package team.themoment.thup.domain.user.service

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.user.dto.ResumeResponse
import team.themoment.thup.domain.user.repository.ResumeRepository
import team.themoment.thup.global.security.userId

@Service
class QueryResumesService(
    private val resumeRepository: ResumeRepository,
) {
    @Transactional(readOnly = true)
    fun execute(user: OAuth2User): List<ResumeResponse> =
        resumeRepository
            .findAllByUserId(user.userId)
            .map(ResumeResponse::from)
}
