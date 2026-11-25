package team.themoment.thup.domain.job.dto

import team.themoment.thup.domain.job.entity.AttachmentJpaEntity
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class JobPostingDetailResponse(
    val id: Long?,
    val companyName: String,
    val description: String,
    val employmentType: EmploymentType,
    val recruitStart: LocalDate,
    val recruitEnd: LocalDate,
    val status: JobPostingStatus,
    val createdAt: LocalDateTime?,
    val positions: List<PositionResponse>,
    val attachments: List<AttachmentResponse>,
) {
    data class AttachmentResponse(
        val id: Long?,
        val fileName: String,
    ) {
        companion object {
            fun from(attachment: AttachmentJpaEntity): AttachmentResponse = AttachmentResponse(attachment.id, attachment.fileName)
        }
    }

    companion object {
        fun of(
            jobPosting: JobPostingJpaEntity,
            positions: List<JobPositionJpaEntity>,
            attachments: List<AttachmentJpaEntity>,
        ): JobPostingDetailResponse =
            JobPostingDetailResponse(
                id = jobPosting.id,
                companyName = jobPosting.companyName,
                description = jobPosting.description,
                employmentType = jobPosting.employmentType,
                recruitStart = jobPosting.recruitStart,
                recruitEnd = jobPosting.recruitEnd,
                status = jobPosting.status,
                createdAt = jobPosting.createdAt,
                positions = positions.map(PositionResponse::from),
                attachments = attachments.map(AttachmentResponse::from),
            )
    }
}
