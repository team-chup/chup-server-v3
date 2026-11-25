package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.transportsubsidy.dto.MyTransportSubsidyResponse
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository
import team.themoment.thup.global.security.userId

@Service
class QueryMyTransportSubsidyApplicationsService(
    private val applicationRepository: TransportSubsidyApplicationRepository,
    private val evidenceRepository: TransportSubsidyEvidenceRepository,
) {
    @Transactional(readOnly = true)
    fun execute(user: OAuth2User): List<MyTransportSubsidyResponse> {
        val applications = applicationRepository.findAllByUserIdOrderByAppliedAtDesc(user.userId)

        val evidencesByApplicationId =
            evidenceRepository
                .findAllByApplicationIdIn(applications.map { it.id!! })
                .groupBy { it.application.id }

        return applications.map { application ->
            MyTransportSubsidyResponse.from(application, evidencesByApplicationId[application.id].orEmpty())
        }
    }
}
