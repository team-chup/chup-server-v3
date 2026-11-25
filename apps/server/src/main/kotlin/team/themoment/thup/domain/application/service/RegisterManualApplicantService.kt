package team.themoment.thup.domain.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.dto.AdminApplicantResponse
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository

@Service
class RegisterManualApplicantService(
    private val applicationRepository: ApplicationRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        userId: Long?,
        companyName: String?,
        sourcePlatform: String?,
        status: ApplicationStatus?,
    ): AdminApplicantResponse {
        if (userId == null) {
            throw ExpectedException("학생을 선택해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (companyName.isNullOrBlank()) {
            throw ExpectedException("회사명을 입력해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (sourcePlatform.isNullOrBlank()) {
            throw ExpectedException("지원 경로(플랫폼)를 입력해야 합니다.", HttpStatus.BAD_REQUEST)
        }
        if (status == null) {
            throw ExpectedException("현재 상태를 선택해야 합니다.", HttpStatus.BAD_REQUEST)
        }

        val student =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (student.role != Role.STUDENT) {
            throw ExpectedException("학생 계정만 등록할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val application =
            ApplicationJpaEntity(
                user = student,
                companyName = companyName,
                applicationSource = ApplicationSource.EXTERNAL,
                sourcePlatform = sourcePlatform,
            )

        if (status != ApplicationStatus.APPLIED) {
            application.updateStatus(status)
        }

        applicationRepository.save(application)

        return AdminApplicantResponse.from(application)
    }
}
