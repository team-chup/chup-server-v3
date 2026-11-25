package team.themoment.thup.domain.application.mail

import team.themoment.thup.domain.application.entity.constant.ApplicationStatus

object ApplicationEmailTemplates {
    fun appliedSubject(companyName: String?): String = "[CHUP] $companyName 지원이 완료되었습니다"

    fun appliedBody(
        companyName: String?,
        positionName: String?,
    ): String =
        "<p>$companyName($positionName) 포지션 지원이 정상적으로 접수되었습니다.</p>\n" +
            "<p>지원 결과는 CHUP에서 확인하실 수 있습니다.</p>\n"

    fun resultSubject(
        companyName: String?,
        status: ApplicationStatus,
    ): String = "[CHUP] $companyName 지원 결과 안내 (${resultLabel(status)})"

    fun resultBody(
        companyName: String?,
        positionName: String?,
        status: ApplicationStatus,
    ): String =
        "<p>$companyName($positionName) 포지션 지원 결과, <strong>${resultLabel(status)}</strong>하셨습니다.</p>\n" +
            "<p>자세한 내용은 CHUP에서 확인하실 수 있습니다.</p>\n"

    private fun resultLabel(status: ApplicationStatus): String =
        when (status) {
            ApplicationStatus.APPLIED -> "지원 접수"
            ApplicationStatus.INTERVIEW_SCHEDULED -> "면접 예정"
            ApplicationStatus.PASSED -> "최종 합격"
            ApplicationStatus.FAILED -> "면접 탈락"
        }
}
