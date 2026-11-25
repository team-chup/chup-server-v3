package team.themoment.thup.domain.job.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPostingRepository
import java.time.LocalDate

@Service
class CloseExpiredJobPostingsService(
    private val jobPostingRepository: JobPostingRepository,
) {
    /**
     * 모집 마감일이 지난 모집중 공고를 마감 처리하고, 마감된 공고의 회사명 목록을 반환한다.
     * recruitEnd 당일까지는 모집 기간에 포함되므로 recruitEnd가 today보다 이전인 공고만 대상으로 한다.
     */
    @Transactional
    fun execute(today: LocalDate): List<String> {
        val expired = jobPostingRepository.findAllByStatusAndRecruitEndBefore(JobPostingStatus.RECRUITING, today)
        expired.forEach { it.updateStatus(JobPostingStatus.CLOSED) }
        return expired.map { it.companyName }
    }
}
