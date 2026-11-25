package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.transportsubsidy.dto.AdminTransportSubsidyResponse
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository

@Service
class QueryTransportSubsidyApplicationsService(
    private val applicationRepository: TransportSubsidyApplicationRepository,
    private val evidenceRepository: TransportSubsidyEvidenceRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long?): List<AdminTransportSubsidyResponse> {
        val applications =
            if (userId == null) {
                applicationRepository.findAllWithUser()
            } else {
                applicationRepository.findAllWithUserByUserId(userId)
            }

        val evidencesByApplicationId =
            evidenceRepository
                .findAllByApplicationIdIn(applications.map { it.id!! })
                .groupBy { it.application.id }

        return applications.map { application ->
            AdminTransportSubsidyResponse.from(application, evidencesByApplicationId[application.id].orEmpty())
        }
    }
}
