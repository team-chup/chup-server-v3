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
import team.themoment.thup.global.security.userId

@Service
class ApproveAdminService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        admin: OAuth2User,
        userId: Long,
    ): UserJpaEntity {
        val adminId = admin.userId

        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (user.role != Role.ADMIN) {
            throw ExpectedException("관리자 승인 대상이 아닙니다.", HttpStatus.BAD_REQUEST)
        }
        if (user.approved) {
            throw ExpectedException("이미 승인된 관리자입니다.", HttpStatus.CONFLICT)
        }

        user.approve()
        try {
            userRepository.saveAndFlush(user)
        } catch (e: OptimisticLockingFailureException) {
            throw ExpectedException("이미 다른 요청에 의해 처리된 사용자입니다.", HttpStatus.CONFLICT)
        }

        log.info("Approved admin. actorId={} targetUserId={}", adminId, userId)
        return user
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApproveAdminService::class.java)
    }
}
