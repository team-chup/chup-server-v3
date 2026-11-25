package team.themoment.thup.domain.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.user.dto.UserSearchResDto
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository

@Service
class QueryUsersService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        role: Role?,
        keyword: String?,
    ): List<UserSearchResDto> =
        userRepository
            .searchByRoleAndKeyword(role, keyword)
            .map(UserSearchResDto::of)
}
