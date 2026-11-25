package team.themoment.thup.domain.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.dto.AdminApplicantResponse
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.application.mail.ApplicationEmailTemplates
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.global.mail.MailService

@Service
class ModifyApplicationResultService(
    private val applicationRepository: ApplicationRepository,
    private val mailService: MailService,
) {
    @Transactional
    fun execute(
        applicationId: Long,
        status: ApplicationStatus?,
    ): AdminApplicantResponse {
        val application =
            applicationRepository.findByIdOrNull(applicationId)
                ?: throw ExpectedException("지원 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        // v2와 동일하게 별도 400 검증 없이, status가 누락되면 서버 에러(500)로 실패한다.
        application.updateStatus(status!!)

        if (application.applicationSource == ApplicationSource.OFFICIAL) {
            val companyName = application.effectiveCompanyName
            val positionName = application.effectivePositionName

            mailService.send(
                application.user.email,
                ApplicationEmailTemplates.resultSubject(companyName, status),
                ApplicationEmailTemplates.resultBody(companyName, positionName, status),
            )
        }

        return AdminApplicantResponse.from(application)
    }
}
