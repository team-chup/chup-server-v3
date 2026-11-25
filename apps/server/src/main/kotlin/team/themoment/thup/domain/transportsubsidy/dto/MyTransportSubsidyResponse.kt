package team.themoment.thup.domain.transportsubsidy.dto

import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyApplicationJpaEntity
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyEvidenceJpaEntity
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus
import java.time.LocalDateTime

data class MyTransportSubsidyResponse(
    val id: Long?,
    val companyName: String,
    val interviewAt: LocalDateTime,
    val status: TransportSubsidyStatus,
    val evidences: List<EvidenceFileResponse>,
    val appliedAt: LocalDateTime?,
    val resultUpdatedAt: LocalDateTime?,
) {
    companion object {
        fun from(
            application: TransportSubsidyApplicationJpaEntity,
            evidences: List<TransportSubsidyEvidenceJpaEntity>,
        ): MyTransportSubsidyResponse =
            MyTransportSubsidyResponse(
                id = application.id,
                companyName = application.companyName,
                interviewAt = application.interviewAt,
                status = application.status,
                evidences = evidences.map(EvidenceFileResponse::from),
                appliedAt = application.appliedAt,
                resultUpdatedAt = application.resultUpdatedAt,
            )
    }
}
