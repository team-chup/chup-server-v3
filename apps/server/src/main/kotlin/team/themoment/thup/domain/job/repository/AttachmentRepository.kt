package team.themoment.thup.domain.job.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.job.entity.AttachmentJpaEntity

interface AttachmentRepository : JpaRepository<AttachmentJpaEntity, Long> {
    fun findAllByJobPostingId(jobPostingId: Long): List<AttachmentJpaEntity>

    fun findByIdAndJobPostingId(
        id: Long,
        jobPostingId: Long,
    ): AttachmentJpaEntity?
}
