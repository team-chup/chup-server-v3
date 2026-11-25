package team.themoment.thup.domain.transportsubsidy.mail

import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus

object TransportSubsidyEmailTemplates {
    fun resultSubject(
        companyName: String,
        status: TransportSubsidyStatus,
    ): String = "[CHUP] $companyName 면접 교통비 지원 ${resultLabel(status)} 안내"

    fun resultBody(
        companyName: String,
        status: TransportSubsidyStatus,
    ): String =
        "<p>$companyName 면접 교통비 지원 신청이 <strong>${resultLabel(status)}</strong>되었습니다.</p>\n" +
            "<p>자세한 내용은 CHUP에서 확인하실 수 있습니다.</p>\n"

    private fun resultLabel(status: TransportSubsidyStatus): String =
        when (status) {
            TransportSubsidyStatus.PENDING -> "대기"
            TransportSubsidyStatus.APPROVED -> "승인"
            TransportSubsidyStatus.REJECTED -> "거절"
        }
}
