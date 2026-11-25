package team.themoment.thup.domain.application.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus

interface ApplicationRepository : JpaRepository<ApplicationJpaEntity, Long> {
    fun existsByUserIdAndJobPostingId(
        userId: Long,
        jobPostingId: Long,
    ): Boolean

    fun existsByJobPositionId(jobPositionId: Long): Boolean

    // 외부 지원 건은 jobPosting이 없어 null이 섞여 나온다
    @Query("select a.jobPosting.id from ApplicationJpaEntity a where a.user.id = :userId")
    fun findJobPostingIdsByUserId(
        @Param("userId") userId: Long,
    ): List<Long?>

    fun countByUserId(userId: Long): Long

    fun countByUserIdAndStatus(
        userId: Long,
        status: ApplicationStatus,
    ): Long

    fun countByStatus(status: ApplicationStatus): Long

    fun countByJobPostingId(jobPostingId: Long): Long

    @Query(
        "select a.jobPosting.id, count(a) from ApplicationJpaEntity a " +
            "where a.jobPosting.id in :jobPostingIds group by a.jobPosting.id",
    )
    fun countGroupedByJobPostingIdIn(
        @Param("jobPostingIds") jobPostingIds: List<Long>,
    ): List<Array<Any>>

    @Query(
        "select a from ApplicationJpaEntity a left join fetch a.jobPosting left join fetch a.jobPosition " +
            "where a.user.id = :userId",
    )
    fun findAllWithJobByUserId(
        @Param("userId") userId: Long,
    ): List<ApplicationJpaEntity>

    @Query(
        "select a from ApplicationJpaEntity a join fetch a.user left join fetch a.jobPosting left join fetch a.jobPosition",
    )
    fun findAllWithDetails(): List<ApplicationJpaEntity>

    // 외부 지원 건은 jobPosting이 null이라 이 조건에서 이미 배제되므로 INNER JOIN FETCH 그대로 둔다.
    @Query(
        "select a from ApplicationJpaEntity a join fetch a.user join fetch a.jobPosting join fetch a.jobPosition " +
            "where a.jobPosting.id = :jobPostingId",
    )
    fun findAllWithDetailsByJobPostingId(
        @Param("jobPostingId") jobPostingId: Long,
    ): List<ApplicationJpaEntity>
}
