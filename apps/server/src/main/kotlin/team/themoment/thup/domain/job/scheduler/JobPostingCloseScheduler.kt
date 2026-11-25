package team.themoment.thup.domain.job.scheduler

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.themoment.thup.domain.job.service.CloseExpiredJobPostingsService
import team.themoment.thup.global.time.AppTimeZone
import java.time.LocalDate

/**
 * 모집 기간이 지난 공고를 자동으로 마감 처리한다.
 *
 * 앱 컨테이너에 TZ가 지정되어 있지 않아 JVM 기본 시간대가 UTC다. 마감 기준일이 하루 어긋나지 않도록
 * 실행 시각(cron zone)과 기준 날짜를 모두 한국 시간으로 고정한다.
 */
@Component
class JobPostingCloseScheduler(
    private val closeExpiredJobPostingsService: CloseExpiredJobPostingsService,
) {
    @Scheduled(cron = "0 0 0 * * *", zone = AppTimeZone.KST_ID)
    fun closeExpiredJobPostings() {
        closeExpired()
    }

    /**
     * 배포·장애로 자정 배치를 놓쳤을 수 있어 기동 직후에도 한 번 보정한다.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun closeExpiredJobPostingsOnStartup() {
        closeExpired()
    }

    private fun closeExpired() {
        val today = LocalDate.now(AppTimeZone.KST)
        val closedCompanyNames = closeExpiredJobPostingsService.execute(today)
        if (closedCompanyNames.isNotEmpty()) {
            log.info(
                "Closed expired job postings. count={}, baseDate={}, companies={}",
                closedCompanyNames.size,
                today,
                closedCompanyNames.joinToString(", "),
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JobPostingCloseScheduler::class.java)
    }
}
