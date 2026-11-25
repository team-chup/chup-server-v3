package team.themoment.thup.domain.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.application.dto.AdminApplicantResponse
import team.themoment.thup.domain.application.repository.ApplicationRepository

@Service
class QueryApplicantsService(
    private val applicationRepository: ApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(jobPostingId: Long?): List<AdminApplicantResponse> {
        val applications =
            if (jobPostingId == null) {
                applicationRepository.findAllWithDetails()
            } else {
                applicationRepository.findAllWithDetailsByJobPostingId(jobPostingId)
            }
        return applications.map(AdminApplicantResponse::from)
    }
}
