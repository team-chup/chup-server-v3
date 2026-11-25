package team.themoment.thup.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.user.entity.ResumeJpaEntity

interface ResumeRepository : JpaRepository<ResumeJpaEntity, Long> {
    fun findAllByUserId(userId: Long): List<ResumeJpaEntity>

    fun findByIdAndUserId(
        id: Long,
        userId: Long,
    ): ResumeJpaEntity?

    fun findAllByIdInAndUserId(
        ids: List<Long>,
        userId: Long,
    ): List<ResumeJpaEntity>

    fun countByUserId(userId: Long): Long
}
