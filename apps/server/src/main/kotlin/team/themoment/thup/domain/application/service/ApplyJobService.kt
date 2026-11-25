package team.themoment.thup.domain.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.dto.ApplicationResponse
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.ApplicationResumeJpaEntity
import team.themoment.thup.domain.application.mail.ApplicationEmailTemplates
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.application.repository.ApplicationResumeRepository
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.domain.user.repository.ResumeRepository
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.mail.MailService
import team.themoment.thup.global.security.userId
import team.themoment.thup.global.time.AppTimeZone
import java.time.LocalDate

@Service
class ApplyJobService(
    private val applicationRepository: ApplicationRepository,
    private val applicationResumeRepository: ApplicationResumeRepository,
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val userRepository: UserRepository,
    private val resumeRepository: ResumeRepository,
    private val mailService: MailService,
) {
    @Transactional
    fun execute(
        user: OAuth2User,
        jobId: Long,
        jobPositionId: Long?,
        resumeIds: List<Long>?,
    ): ApplicationResponse {
        val userId = user.userId

        if (resumeIds.isNullOrEmpty()) {
            throw ExpectedException("이력서를 하나 이상 선택해야 합니다.", HttpStatus.BAD_REQUEST)
        }

        if (applicationRepository.existsByUserIdAndJobPostingId(userId, jobId)) {
            throw ExpectedException("이미 지원한 공고입니다.", HttpStatus.CONFLICT)
        }

        val foundUser =
            userRepository.findByIdOrNull(userId)
                ?: throw ExpectedException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        val jobPosting =
            jobPostingRepository.findByIdOrNull(jobId)
                ?: throw ExpectedException("공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        // 자동 마감 배치가 아직 돌지 않았거나 실패한 경우에도 막을 수 있도록 상태와 모집 마감일을 함께 확인한다
        if (jobPosting.status == JobPostingStatus.CLOSED ||
            jobPosting.recruitEnd.isBefore(LocalDate.now(AppTimeZone.KST))
        ) {
            throw ExpectedException("마감된 공고입니다.", HttpStatus.CONFLICT)
        }

        // v2와 동일하게 jobPositionId 누락은 별도 400 검증 없이 서버 에러(500)로 실패한다.
        val jobPosition =
            jobPositionRepository.findByIdOrNull(jobPositionId!!)
                ?: throw ExpectedException("포지션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val resumes = resumeRepository.findAllByIdInAndUserId(resumeIds, userId)
        if (resumes.size != resumeIds.size) {
            throw ExpectedException("선택한 이력서를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        val application =
            applicationRepository.save(
                ApplicationJpaEntity(
                    user = foundUser,
                    jobPosting = jobPosting,
                    jobPosition = jobPosition,
                ),
            )

        resumes.forEach { resume ->
            applicationResumeRepository.save(
                ApplicationResumeJpaEntity(
                    application = application,
                    resumeSnapshotUrl = resume.fileUrl,
                    resumeFileName = resume.fileName,
                ),
            )
        }

        mailService.send(
            foundUser.email,
            ApplicationEmailTemplates.appliedSubject(jobPosting.companyName),
            ApplicationEmailTemplates.appliedBody(jobPosting.companyName, jobPosition.name),
        )

        return ApplicationResponse.from(application)
    }
}
