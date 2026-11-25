package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.transportsubsidy.dto.AdminTransportSubsidyResponse
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus
import team.themoment.thup.domain.transportsubsidy.mail.TransportSubsidyEmailTemplates
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.mail.MailService

@Service
class ModifyTransportSubsidyResultService(
    private val applicationRepository: TransportSubsidyApplicationRepository,
    private val evidenceRepository: TransportSubsidyEvidenceRepository,
    private val userRepository: UserRepository,
    private val mailService: MailService,
) {
    @Transactional
    fun execute(
        applicationId: Long,
        status: TransportSubsidyStatus?,
    ): AdminTransportSubsidyResponse {
        if (status == null || (status != TransportSubsidyStatus.APPROVED && status != TransportSubsidyStatus.REJECTED)) {
            throw ExpectedException("승인 또는 거절 상태만 지정할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        // 동일 신청 건에 대한 동시 승인/거절 요청이 서로의 결과를 조용히 덮어쓰지 않도록,
        // 신청 건 row에 락을 건 뒤 그 안에서 상태를 확인·변경한다.
        val application =
            applicationRepository.findByIdForUpdate(applicationId)
                ?: throw ExpectedException("신청 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (application.status != TransportSubsidyStatus.PENDING) {
            throw ExpectedException("이미 처리된 신청입니다.", HttpStatus.CONFLICT)
        }

        val userId = application.user.id!!

        if (status == TransportSubsidyStatus.APPROVED) {
            // 동시 승인 요청이 카운트 체크를 동시에 통과해 승인 횟수 제한을 넘기지 않도록,
            // 유저 row에 락을 건 뒤 그 안에서 카운트를 확인한다.
            userRepository.findByIdForUpdate(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

            if (applicationRepository.countByUserIdAndStatus(userId, TransportSubsidyStatus.APPROVED) >= MAX_APPROVED_COUNT) {
                throw ExpectedException(
                    "이미 면접 교통비 지원을 ${MAX_APPROVED_COUNT}회 모두 사용했습니다.",
                    HttpStatus.CONFLICT,
                )
            }
        }

        application.updateStatus(status)

        mailService.send(
            application.user.email,
            TransportSubsidyEmailTemplates.resultSubject(application.companyName, status),
            TransportSubsidyEmailTemplates.resultBody(application.companyName, status),
        )

        val evidences = evidenceRepository.findAllByApplicationId(applicationId)
        return AdminTransportSubsidyResponse.from(application, evidences)
    }

    companion object {
        private const val MAX_APPROVED_COUNT = 2
    }
}
