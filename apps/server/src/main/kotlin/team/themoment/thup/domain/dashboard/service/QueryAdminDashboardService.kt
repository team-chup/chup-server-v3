package team.themoment.thup.domain.dashboard.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.dashboard.dto.response.AdminDashboardResDto
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPostingRepository

@Service
class QueryAdminDashboardService(
    private val jobPostingRepository: JobPostingRepository,
    private val applicationRepository: ApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): AdminDashboardResDto =
        AdminDashboardResDto(
            openJobs = jobPostingRepository.countByStatus(JobPostingStatus.RECRUITING),
            totalApplicants = applicationRepository.count(),
            pending = applicationRepository.countByStatus(ApplicationStatus.APPLIED),
            passed = applicationRepository.countByStatus(ApplicationStatus.PASSED),
        )
}
