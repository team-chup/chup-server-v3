package team.themoment.thup.domain.user.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByEmail(email: String): UserJpaEntity?

    fun findAllByRoleAndApprovedFalseOrderByCreatedAtAsc(role: Role): List<UserJpaEntity>

    fun findAllByRoleAndGrade(
        role: Role,
        grade: Int,
    ): List<UserJpaEntity>

    @Query(
        "select u from UserJpaEntity u " +
            "where (:role is null or u.role = :role) " +
            "and (:keyword is null or :keyword = '' " +
            "     or lower(u.name) like lower(concat('%', :keyword, '%')) " +
            "     or u.studentId like concat('%', :keyword, '%')) " +
            "order by u.name asc",
    )
    fun searchByRoleAndKeyword(
        @Param("role") role: Role?,
        @Param("keyword") keyword: String?,
    ): List<UserJpaEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserJpaEntity u where u.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): UserJpaEntity?
}
