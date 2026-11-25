package team.themoment.thup.domain.job.dto

import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminJobPostingResponse(
    val id: Long?,
    val companyName: String,
    val description: String,
    val employmentType: EmploymentType,
    val recruitStart: LocalDate,
    val recruitEnd: LocalDate,
    val status: JobPostingStatus,
    val createdAt: LocalDateTime?,
    val applicantCount: Long,
    val positions: List<PositionResponse>,
) {
    companion object {
        fun of(
            jobPosting: JobPostingJpaEntity,
            applicantCount: Long,
            positions: List<JobPositionJpaEntity>,
        ): AdminJobPostingResponse =
            AdminJobPostingResponse(
                id = jobPosting.id,
                companyName = jobPosting.companyName,
                description = jobPosting.description,
                employmentType = jobPosting.employmentType,
                recruitStart = jobPosting.recruitStart,
                recruitEnd = jobPosting.recruitEnd,
                status = jobPosting.status,
                createdAt = jobPosting.createdAt,
                applicantCount = applicantCount,
                positions = positions.map(PositionResponse::from),
            )
    }
}
