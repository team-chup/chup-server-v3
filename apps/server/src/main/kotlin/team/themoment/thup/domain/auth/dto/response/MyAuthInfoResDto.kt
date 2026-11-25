package team.themoment.thup.domain.auth.dto.response

import team.themoment.thup.domain.user.entity.constant.Role

data class MyAuthInfoResDto(
    val role: Role,
    val approved: Boolean,
)
