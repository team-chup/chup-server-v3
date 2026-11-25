package team.themoment.thup.domain.transportsubsidy.dto

data class TransportSubsidyStudentSummaryResponse(
    val userId: Long?,
    val name: String,
    val studentId: String?,
    val approvedCount: Long,
    val totalCount: Long,
)
