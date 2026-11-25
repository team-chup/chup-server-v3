package team.themoment.thup.domain.transportsubsidy.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.transportsubsidy.entity.TransportSubsidyApplicationJpaEntity
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.mail.MailService
import team.themoment.thup.support.user
import team.themoment.thup.support.withId
import java.time.LocalDateTime

class ModifyTransportSubsidyResultServiceTest :
    DescribeSpec({
        val applicationRepository = mockk<TransportSubsidyApplicationRepository>()
        val evidenceRepository = mockk<TransportSubsidyEvidenceRepository>()
        val userRepository = mockk<UserRepository>()
        val mailService = mockk<MailService>()

        val service =
            ModifyTransportSubsidyResultService(applicationRepository, evidenceRepository, userRepository, mailService)

        val student = user(id = 1L)

        fun pendingApplication(status: TransportSubsidyStatus = TransportSubsidyStatus.PENDING) =
            TransportSubsidyApplicationJpaEntity(
                user = student,
                companyName = "더모먼트",
                interviewAt = LocalDateTime.now(),
                status = status,
            ).withId(7L)

        beforeEach {
            clearAllMocks(answers = false)
            every { evidenceRepository.findAllByApplicationId(7L) } returns emptyList()
            every { userRepository.findByIdForUpdate(1L) } returns student
            justRun { mailService.send(any(), any(), any()) }
        }

        describe("execute") {
            it("PENDING이나 null 상태로는 변경할 수 없다") {
                listOf(TransportSubsidyStatus.PENDING, null).forEach { status ->
                    val exception = shouldThrow<ExpectedException> { service.execute(7L, status) }
                    exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                }
            }

            it("이미 처리된 신청이면 409를 던진다") {
                every { applicationRepository.findByIdForUpdate(7L) } returns
                    pendingApplication(TransportSubsidyStatus.REJECTED)

                val exception = shouldThrow<ExpectedException> { service.execute(7L, TransportSubsidyStatus.APPROVED) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
            }

            it("이미 2회 승인된 학생은 추가 승인할 수 없다") {
                every { applicationRepository.findByIdForUpdate(7L) } returns pendingApplication()
                every { applicationRepository.countByUserIdAndStatus(1L, TransportSubsidyStatus.APPROVED) } returns 2L

                val exception = shouldThrow<ExpectedException> { service.execute(7L, TransportSubsidyStatus.APPROVED) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
                verify(exactly = 0) { mailService.send(any(), any(), any()) }
            }

            it("승인하면 상태와 처리 시각을 갱신하고 결과 메일을 보낸다") {
                every { applicationRepository.findByIdForUpdate(7L) } returns pendingApplication()
                every { applicationRepository.countByUserIdAndStatus(1L, TransportSubsidyStatus.APPROVED) } returns 1L

                val response = service.execute(7L, TransportSubsidyStatus.APPROVED)

                response.status shouldBe TransportSubsidyStatus.APPROVED
                response.resultUpdatedAt.shouldNotBeNull()
                verify { mailService.send(student.email, "[CHUP] 더모먼트 면접 교통비 지원 승인 안내", any()) }
            }

            it("거절은 승인 횟수 제한을 확인하지 않는다") {
                every { applicationRepository.findByIdForUpdate(7L) } returns pendingApplication()

                val response = service.execute(7L, TransportSubsidyStatus.REJECTED)

                response.status shouldBe TransportSubsidyStatus.REJECTED
                verify(exactly = 0) { applicationRepository.countByUserIdAndStatus(any(), any()) }
            }
        }
    })
