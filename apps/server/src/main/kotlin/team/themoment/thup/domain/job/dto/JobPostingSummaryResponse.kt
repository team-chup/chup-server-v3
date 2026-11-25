package team.themoment.thup.domain.job.dto

import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import java.time.LocalDate

data class JobPostingSummaryResponse(
    val id: Long?,
    val companyName: String,
    val employmentType: EmploymentType,
    val recruitEnd: LocalDate,
    val positions: List<PositionResponse>,
) {
    companion object {
        fun of(
            jobPosting: JobPostingJpaEntity,
            positions: List<JobPositionJpaEntity>,
        ): JobPostingSummaryResponse =
            JobPostingSummaryResponse(
                id = jobPosting.id,
                companyName = jobPosting.companyName,
                employmentType = jobPosting.employmentType,
                recruitEnd = jobPosting.recruitEnd,
                positions = positions.map(PositionResponse::from),
            )
    }
}
