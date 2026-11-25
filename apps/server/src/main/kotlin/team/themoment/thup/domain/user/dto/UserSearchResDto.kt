package team.themoment.thup.domain.user.dto

import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role

data class UserSearchResDto(
    val id: Long?,
    val name: String,
    val studentId: String?,
    val email: String,
    val role: Role,
) {
    companion object {
        fun of(user: UserJpaEntity): UserSearchResDto =
            UserSearchResDto(
                id = user.id,
                name = user.name,
                studentId = user.studentId,
                email = user.email,
                role = user.role,
            )
    }
}
