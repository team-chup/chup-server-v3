package team.themoment.thup.domain.job.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import java.time.LocalDate

interface JobPostingRepository : JpaRepository<JobPostingJpaEntity, Long> {
    fun findAllByStatus(status: JobPostingStatus): List<JobPostingJpaEntity>

    fun findAllByStatusAndRecruitEndBefore(
        status: JobPostingStatus,
        date: LocalDate,
    ): List<JobPostingJpaEntity>

    fun findAllByCompanyNameContainingIgnoreCase(companyName: String): List<JobPostingJpaEntity>

    fun findAllByStatusOrderByRecruitEndAsc(status: JobPostingStatus): List<JobPostingJpaEntity>

    fun countByStatus(status: JobPostingStatus): Long
}
