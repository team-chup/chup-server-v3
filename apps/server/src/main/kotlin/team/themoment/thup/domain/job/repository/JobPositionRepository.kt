package team.themoment.thup.domain.job.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity

interface JobPositionRepository : JpaRepository<JobPositionJpaEntity, Long> {
    fun findAllByJobPostingId(jobPostingId: Long): List<JobPositionJpaEntity>

    fun findAllByJobPostingIdIn(jobPostingIds: List<Long>): List<JobPositionJpaEntity>
}
