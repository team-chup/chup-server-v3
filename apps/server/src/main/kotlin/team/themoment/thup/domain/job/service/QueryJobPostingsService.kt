package team.themoment.thup.domain.job.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.dto.JobPostingSummaryResponse
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository

@Service
class QueryJobPostingsService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        q: String?,
        position: String?,
        employmentType: EmploymentType?,
        sort: String?,
    ): List<JobPostingSummaryResponse> {
        val jobPostings = jobPostingRepository.findAllByStatus(JobPostingStatus.RECRUITING)

        val positionsByJobId =
            jobPositionRepository
                .findAllByJobPostingIdIn(jobPostings.map { it.id!! })
                .groupBy { it.jobPosting.id }

        val keyword = normalize(q)
        val positionFilter = normalize(position)

        var filtered =
            jobPostings
                .filter { job -> employmentType == null || job.employmentType == employmentType }
                .filter { job ->
                    positionFilter == null ||
                        positionsByJobId[job.id].orEmpty().any { it.name.equals(positionFilter, ignoreCase = true) }
                }.filter { job ->
                    keyword == null ||
                        job.companyName.lowercase().contains(keyword) ||
                        positionsByJobId[job.id].orEmpty().any { it.name.lowercase().contains(keyword) }
                }

        if (!sort.isNullOrBlank()) {
            if (!SORT_DEADLINE_ASC.equals(sort, ignoreCase = true)) {
                throw ExpectedException("지원하지 않는 정렬 옵션입니다. (deadline_asc만 가능)", HttpStatus.BAD_REQUEST)
            }
            filtered = filtered.sortedBy { it.recruitEnd }
        }

        return filtered.map { job -> JobPostingSummaryResponse.of(job, positionsByJobId[job.id].orEmpty()) }
    }

    private fun normalize(value: String?): String? = if (value.isNullOrBlank()) null else value.trim().lowercase()

    companion object {
        private const val SORT_DEADLINE_ASC = "deadline_asc"
    }
}
