package team.themoment.thup.domain.transportsubsidy.dto

import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyEvidenceJpaEntity

data class EvidenceFileResponse(
    val id: Long?,
    val fileName: String,
    val fileSize: Int,
) {
    companion object {
        fun from(evidence: TransportSubsidyEvidenceJpaEntity): EvidenceFileResponse =
            EvidenceFileResponse(evidence.id, evidence.fileName, evidence.fileSize)
    }
}
