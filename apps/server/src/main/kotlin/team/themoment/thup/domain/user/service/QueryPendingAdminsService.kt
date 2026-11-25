package team.themoment.thup.domain.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.user.dto.PendingAdminResDto
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository

@Service
class QueryPendingAdminsService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<PendingAdminResDto> =
        userRepository
            .findAllByRoleAndApprovedFalseOrderByCreatedAtAsc(Role.ADMIN)
            .map(PendingAdminResDto::of)
}
