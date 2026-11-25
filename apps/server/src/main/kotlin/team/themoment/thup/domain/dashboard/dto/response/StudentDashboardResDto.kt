package team.themoment.thup.domain.dashboard.dto.response

data class StudentDashboardResDto(
    val openJobs: Long,
    val myApplications: Long,
    val passed: Long,
    val recommendedJobs: List<RecommendedJobResDto>,
)
