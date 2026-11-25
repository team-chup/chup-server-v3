package team.themoment.thup.domain.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.ApplicationResumeJpaEntity
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.application.repository.ApplicationResumeRepository
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.domain.user.entity.ResumeJpaEntity
import team.themoment.thup.domain.user.repository.ResumeRepository
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.mail.MailService
import team.themoment.thup.support.jobPosition
import team.themoment.thup.support.jobPosting
import team.themoment.thup.support.oAuth2User
import team.themoment.thup.support.user
import team.themoment.thup.support.withId
import java.time.LocalDate
import java.util.Optional

class ApplyJobServiceTest :
    DescribeSpec({
        val applicationRepository = mockk<ApplicationRepository>()
        val applicationResumeRepository = mockk<ApplicationResumeRepository>()
        val jobPostingRepository = mockk<JobPostingRepository>()
        val jobPositionRepository = mockk<JobPositionRepository>()
        val userRepository = mockk<UserRepository>()
        val resumeRepository = mockk<ResumeRepository>()
        val mailService = mockk<MailService>()

        val service =
            ApplyJobService(
                applicationRepository,
                applicationResumeRepository,
                jobPostingRepository,
                jobPositionRepository,
                userRepository,
                resumeRepository,
                mailService,
            )

        val student = user(id = 1L)
        val principal = oAuth2User(1L)

        beforeEach {
            clearAllMocks(answers = false)
            every { applicationRepository.existsByUserIdAndJobPostingId(any(), any()) } returns false
            every { userRepository.findById(1L) } returns Optional.of(student)
        }

        describe("execute") {
            it("이력서를 선택하지 않으면 400을 던진다") {
                val exception = shouldThrow<ExpectedException> { service.execute(principal, 10L, 100L, emptyList()) }

                exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            it("이미 지원한 공고면 409를 던진다") {
                every { applicationRepository.existsByUserIdAndJobPostingId(1L, 10L) } returns true

                val exception = shouldThrow<ExpectedException> { service.execute(principal, 10L, 100L, listOf(1L)) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
            }

            it("상태가 CLOSED인 공고면 409를 던진다") {
                every { jobPostingRepository.findById(10L) } returns Optional.of(jobPosting(status = JobPostingStatus.CLOSED))

                val exception = shouldThrow<ExpectedException> { service.execute(principal, 10L, 100L, listOf(1L)) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
            }

            it("마감 배치가 돌기 전이라도 모집 마감일이 지났으면 409를 던진다") {
                every { jobPostingRepository.findById(10L) } returns Optional.of(jobPosting(recruitEnd = LocalDate.now().minusDays(2)))

                val exception = shouldThrow<ExpectedException> { service.execute(principal, 10L, 100L, listOf(1L)) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
            }

            it("내 이력서가 아닌 id가 섞여 있으면 400을 던진다") {
                val posting = jobPosting()
                every { jobPostingRepository.findById(10L) } returns Optional.of(posting)
                every { jobPositionRepository.findById(100L) } returns Optional.of(jobPosition(100L, posting, "백엔드"))
                every { resumeRepository.findAllByIdInAndUserId(listOf(1L, 2L), 1L) } returns
                    listOf(ResumeJpaEntity(student, "이력서.pdf", "resumes/1/a.pdf", 10).withId(1L))

                val exception = shouldThrow<ExpectedException> { service.execute(principal, 10L, 100L, listOf(1L, 2L)) }

                exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            it("정상 지원 시 이력서 스냅샷을 저장하고 접수 메일을 보낸다") {
                val posting = jobPosting()
                every { jobPostingRepository.findById(10L) } returns Optional.of(posting)
                every { jobPositionRepository.findById(100L) } returns Optional.of(jobPosition(100L, posting, "백엔드"))
                every { resumeRepository.findAllByIdInAndUserId(listOf(1L), 1L) } returns
                    listOf(ResumeJpaEntity(student, "이력서.pdf", "resumes/1/a.pdf", 10).withId(1L))
                every { applicationRepository.save(any()) } answers { firstArg<ApplicationJpaEntity>().withId(500L) }
                every { applicationResumeRepository.save(any()) } answers { firstArg() }
                justRun { mailService.send(any(), any(), any()) }

                val response = service.execute(principal, 10L, 100L, listOf(1L))

                response.id shouldBe 500L
                response.companyName shouldBe "더모먼트"
                response.positionName shouldBe "백엔드"
                verify(exactly = 1) {
                    applicationResumeRepository.save(
                        match<ApplicationResumeJpaEntity> { it.resumeSnapshotUrl == "resumes/1/a.pdf" },
                    )
                }
                verify(exactly = 1) {
                    mailService.send(student.email, "[CHUP] 더모먼트 지원이 완료되었습니다", any())
                }
            }
        }
    })
