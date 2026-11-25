package team.themoment.thup.domain.transportsubsidy.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyApplicationJpaEntity
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus

interface TransportSubsidyApplicationRepository : JpaRepository<TransportSubsidyApplicationJpaEntity, Long> {
    fun countByUserIdAndStatus(
        userId: Long,
        status: TransportSubsidyStatus,
    ): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TransportSubsidyApplicationJpaEntity a where a.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): TransportSubsidyApplicationJpaEntity?

    fun findAllByUserIdOrderByAppliedAtDesc(userId: Long): List<TransportSubsidyApplicationJpaEntity>

    @Query("select a from TransportSubsidyApplicationJpaEntity a join fetch a.user order by a.appliedAt desc")
    fun findAllWithUser(): List<TransportSubsidyApplicationJpaEntity>

    @Query(
        "select a from TransportSubsidyApplicationJpaEntity a join fetch a.user " +
            "where a.user.id = :userId order by a.appliedAt desc",
    )
    fun findAllWithUserByUserId(
        @Param("userId") userId: Long,
    ): List<TransportSubsidyApplicationJpaEntity>

    @Query(
        "select a.user.id, count(a) from TransportSubsidyApplicationJpaEntity a " +
            "where a.status = :status group by a.user.id",
    )
    fun countGroupedByUserIdAndStatus(
        @Param("status") status: TransportSubsidyStatus,
    ): List<Array<Any>>

    @Query("select a.user.id, count(a) from TransportSubsidyApplicationJpaEntity a group by a.user.id")
    fun countGroupedByUserId(): List<Array<Any>>
}
