package team.themoment.thup.domain.dashboard.service

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.dashboard.dto.response.RecommendedJobResDto
import team.themoment.thup.domain.dashboard.dto.response.StudentDashboardResDto
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.global.security.userId
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class QueryStudentDashboardService(
    private val jobPostingRepository: JobPostingRepository,
    private val applicationRepository: ApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(user: OAuth2User): StudentDashboardResDto {
        val userId = user.userId

        val openJobs = jobPostingRepository.countByStatus(JobPostingStatus.RECRUITING)
        val myApplications = applicationRepository.countByUserId(userId)
        val passed = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.PASSED)

        val appliedJobPostingIds = applicationRepository.findJobPostingIdsByUserId(userId).toSet()
        val recommendedJobs =
            jobPostingRepository
                .findAllByStatusOrderByRecruitEndAsc(JobPostingStatus.RECRUITING)
                .asSequence()
                .filter { it.id !in appliedJobPostingIds }
                .take(RECOMMENDED_JOB_LIMIT)
                .map { job ->
                    RecommendedJobResDto(
                        id = job.id,
                        companyName = job.companyName,
                        dDay = ChronoUnit.DAYS.between(LocalDate.now(), job.recruitEnd),
                    )
                }.toList()

        return StudentDashboardResDto(openJobs, myApplications, passed, recommendedJobs)
    }

    companion object {
        private const val RECOMMENDED_JOB_LIMIT = 3
    }
}
