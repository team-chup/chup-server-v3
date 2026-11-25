package team.themoment.thup.domain.transportsubsidy.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyEvidenceJpaEntity

interface TransportSubsidyEvidenceRepository : JpaRepository<TransportSubsidyEvidenceJpaEntity, Long> {
    fun findAllByApplicationId(applicationId: Long): List<TransportSubsidyEvidenceJpaEntity>

    fun findAllByApplicationIdIn(applicationIds: List<Long>): List<TransportSubsidyEvidenceJpaEntity>
}
