package team.themoment.thup.domain.auth.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.auth.dto.response.MyAuthInfoResDto
import team.themoment.thup.domain.user.repository.UserRepository

@Service
class QueryMyAuthInfoService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): MyAuthInfoResDto {
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        return MyAuthInfoResDto(user.role, user.approved)
    }
}
