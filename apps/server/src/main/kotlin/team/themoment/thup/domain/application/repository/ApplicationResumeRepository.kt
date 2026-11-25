package team.themoment.thup.domain.application.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.application.entity.ApplicationResumeJpaEntity

interface ApplicationResumeRepository : JpaRepository<ApplicationResumeJpaEntity, Long> {
    fun findAllByApplicationId(applicationId: Long): List<ApplicationResumeJpaEntity>

    fun findAllByApplicationIdIn(applicationIds: List<Long>): List<ApplicationResumeJpaEntity>
}
