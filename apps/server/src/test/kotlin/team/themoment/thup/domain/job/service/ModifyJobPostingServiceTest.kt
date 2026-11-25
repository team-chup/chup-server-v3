package team.themoment.thup.domain.job.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.job.entity.AttachmentJpaEntity
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.constant.AttachmentFileType
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.global.storage.FileStorageService
import team.themoment.thup.global.storage.StoredFile
import team.themoment.thup.support.jobPosition
import team.themoment.thup.support.jobPosting
import team.themoment.thup.support.withId
import java.time.LocalDate
import java.util.Optional

class ModifyJobPostingServiceTest :
    DescribeSpec({
        val jobPostingRepository = mockk<JobPostingRepository>()
        val jobPositionRepository = mockk<JobPositionRepository>()
        val attachmentRepository = mockk<AttachmentRepository>()
        val applicationRepository = mockk<ApplicationRepository>()
        val fileStorageService = mockk<FileStorageService>()

        val service =
            ModifyJobPostingService(
                jobPostingRepository,
                jobPositionRepository,
                attachmentRepository,
                applicationRepository,
                fileStorageService,
            )

        val posting = jobPosting(id = 10L)

        fun attachment(id: Long) =
            AttachmentJpaEntity(posting, "file$id.pdf", "job-attachments/10/$id.pdf", AttachmentFileType.PDF, 1).withId(id)

        fun pdf(name: String) = MockMultipartFile("attachments", name, "application/pdf", byteArrayOf(1))

        fun modify(
            positionNames: List<String>? = null,
            retainedAttachmentIds: List<Long>? = null,
            attachments: List<MockMultipartFile>? = null,
        ) = service.execute(
            10L,
            "더모먼트",
            "설명",
            EmploymentType.FULL_TIME,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            positionNames,
            retainedAttachmentIds,
            attachments,
        )

        beforeEach {
            clearAllMocks(answers = false)
            every { jobPostingRepository.findById(10L) } returns Optional.of(posting)
            every { jobPositionRepository.findAllByJobPostingId(10L) } returns emptyList()
            every { attachmentRepository.findAllByJobPostingId(10L) } returns listOf(attachment(1L), attachment(2L))
            justRun { attachmentRepository.deleteAll(any<Iterable<AttachmentJpaEntity>>()) }
            justRun { fileStorageService.delete(any()) }
            every { attachmentRepository.save(any()) } answers { firstArg() }
            every { fileStorageService.store(any(), any()) } answers {
                StoredFile("job-attachments/10/new.pdf", firstArg<MockMultipartFile>().originalFilename, 1L)
            }
        }

        describe("포지션 동기화") {
            it("지원자가 있는 포지션을 빼려고 하면 409와 함께 포지션명을 알려준다") {
                every { jobPositionRepository.findAllByJobPostingId(10L) } returns
                    listOf(jobPosition(11L, posting, "백엔드"), jobPosition(12L, posting, "프론트엔드"))
                every { applicationRepository.existsByJobPositionId(12L) } returns true

                val exception = shouldThrow<ExpectedException> { modify(positionNames = listOf("백엔드")) }

                exception.statusCode shouldBe HttpStatus.CONFLICT
                exception.message!! shouldContain "프론트엔드"
            }

            it("새 이름만 추가하고 공백 이름은 무시한다") {
                every { jobPositionRepository.findAllByJobPostingId(10L) } returns listOf(jobPosition(11L, posting, "백엔드"))
                every { jobPositionRepository.save(any()) } answers { firstArg() }

                modify(positionNames = listOf(" 백엔드 ", "AI", "  "))

                verify(exactly = 1) { jobPositionRepository.save(match<JobPositionJpaEntity> { it.name == "AI" }) }
            }
        }

        describe("첨부파일 수정") {
            it("retainedAttachmentIds 없이 새 파일만 보내면 기존 파일은 유지한다") {
                modify(attachments = listOf(pdf("new.pdf")))

                verify { attachmentRepository.deleteAll(emptyList<AttachmentJpaEntity>()) }
                verify(exactly = 1) { attachmentRepository.save(any()) }
            }

            it("retainedAttachmentIds=[]이면 기존 파일을 모두 삭제한다") {
                modify(retainedAttachmentIds = emptyList())

                verify { fileStorageService.delete("job-attachments/10/1.pdf") }
                verify { fileStorageService.delete("job-attachments/10/2.pdf") }
            }

            it("유지할 파일과 새 파일 합계가 5개를 넘으면 413을 던진다") {
                val exception =
                    shouldThrow<ExpectedException> {
                        modify(attachments = (1..4).map { pdf("new$it.pdf") })
                    }

                exception.statusCode shouldBe HttpStatus.PAYLOAD_TOO_LARGE
                verify(exactly = 0) { fileStorageService.store(any(), any()) }
            }
        }
    })
