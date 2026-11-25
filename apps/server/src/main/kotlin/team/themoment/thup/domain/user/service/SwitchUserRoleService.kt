package team.themoment.thup.domain.user.service

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.config.DevToolsProperties
import team.themoment.thup.global.security.userId

@Service
class SwitchUserRoleService(
    private val userRepository: UserRepository,
    private val devToolsProperties: DevToolsProperties,
) {
    @Transactional
    fun execute(
        admin: OAuth2User,
        userId: Long,
        role: Role?,
    ): UserJpaEntity {
        if (!devToolsProperties.enabled) {
            throw ExpectedException("개발 환경에서만 사용할 수 있습니다.", HttpStatus.NOT_FOUND)
        }
        if (role == null) {
            throw ExpectedException("변경할 역할을 선택해야 합니다.", HttpStatus.BAD_REQUEST)
        }

        val adminId = admin.userId

        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        user.switchRole(role)
        try {
            userRepository.saveAndFlush(user)
        } catch (e: OptimisticLockingFailureException) {
            throw ExpectedException("이미 다른 요청에 의해 처리된 사용자입니다.", HttpStatus.CONFLICT)
        }

        log.info("[DEV] Switched user role. actorId={} targetUserId={} role={}", adminId, userId, role)
        return user
    }

    companion object {
        private val log = LoggerFactory.getLogger(SwitchUserRoleService::class.java)
    }
}
