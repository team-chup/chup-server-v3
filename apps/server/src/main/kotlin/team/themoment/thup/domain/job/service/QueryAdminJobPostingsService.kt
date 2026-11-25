package team.themoment.thup.domain.job.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.job.dto.AdminJobPostingResponse
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository

@Service
class QueryAdminJobPostingsService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val applicationRepository: ApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(q: String?): List<AdminJobPostingResponse> {
        val jobPostings =
            if (q.isNullOrBlank()) {
                jobPostingRepository.findAll()
            } else {
                jobPostingRepository.findAllByCompanyNameContainingIgnoreCase(q.trim())
            }

        val jobPostingIds = jobPostings.map { it.id!! }

        val applicantCountByJobId =
            applicationRepository
                .countGroupedByJobPostingIdIn(jobPostingIds)
                .associate { row -> row[0] as Long to row[1] as Long }

        val positionsByJobId =
            jobPositionRepository
                .findAllByJobPostingIdIn(jobPostingIds)
                .groupBy { it.jobPosting.id }

        return jobPostings.map { jobPosting ->
            AdminJobPostingResponse.of(
                jobPosting,
                applicantCountByJobId[jobPosting.id] ?: 0L,
                positionsByJobId[jobPosting.id] ?: emptyList(),
            )
        }
    }
}
