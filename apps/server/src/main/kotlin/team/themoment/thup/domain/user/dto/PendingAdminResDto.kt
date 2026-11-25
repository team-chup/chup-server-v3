package team.themoment.thup.domain.user.dto

import team.themoment.thup.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

data class PendingAdminResDto(
    val id: Long?,
    val name: String,
    val email: String,
    val studentId: String?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun of(user: UserJpaEntity): PendingAdminResDto =
            PendingAdminResDto(
                id = user.id,
                name = user.name,
                email = user.email,
                studentId = user.studentId,
                createdAt = user.createdAt,
            )
    }
}
