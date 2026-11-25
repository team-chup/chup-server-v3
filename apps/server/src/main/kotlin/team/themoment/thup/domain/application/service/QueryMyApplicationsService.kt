package team.themoment.thup.domain.application.service

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.application.dto.ApplicationResponse
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.global.security.userId

@Service
class QueryMyApplicationsService(
    private val applicationRepository: ApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(user: OAuth2User): List<ApplicationResponse> =
        applicationRepository
            .findAllWithJobByUserId(user.userId)
            .map(ApplicationResponse::from)
}
