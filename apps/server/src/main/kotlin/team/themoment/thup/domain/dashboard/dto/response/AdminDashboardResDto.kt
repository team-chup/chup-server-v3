package team.themoment.thup.domain.dashboard.dto.response

data class AdminDashboardResDto(
    val openJobs: Long,
    val totalApplicants: Long,
    val pending: Long,
    val passed: Long,
)
