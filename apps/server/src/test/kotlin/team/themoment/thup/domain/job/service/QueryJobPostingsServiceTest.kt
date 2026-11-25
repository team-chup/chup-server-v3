package team.themoment.thup.domain.job.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository
import team.themoment.thup.support.jobPosition
import team.themoment.thup.support.jobPosting
import java.time.LocalDate

class QueryJobPostingsServiceTest :
    DescribeSpec({
        val jobPostingRepository = mockk<JobPostingRepository>()
        val jobPositionRepository = mockk<JobPositionRepository>()
        val service = QueryJobPostingsService(jobPostingRepository, jobPositionRepository)

        val later =
            jobPosting(id = 1L, companyName = "Later Corp", recruitEnd = LocalDate.now().plusDays(10))
        val sooner =
            jobPosting(
                id = 2L,
                companyName = "Sooner Inc",
                employmentType = EmploymentType.INTERN,
                recruitEnd = LocalDate.now().plusDays(2),
            )

        every { jobPostingRepository.findAllByStatus(JobPostingStatus.RECRUITING) } returns listOf(later, sooner)
        every { jobPositionRepository.findAllByJobPostingIdIn(listOf(1L, 2L)) } returns
            listOf(
                jobPosition(11L, later, "Backend"),
                jobPosition(21L, sooner, "Frontend"),
            )

        describe("execute") {
            it("조건이 없으면 조회 순서 그대로 반환한다") {
                service.execute(null, null, null, null).map { it.id } shouldContainExactly listOf(1L, 2L)
            }

            it("고용 형태로 필터링한다") {
                service.execute(null, null, EmploymentType.INTERN, null).map { it.id } shouldContainExactly listOf(2L)
            }

            it("포지션명은 대소문자를 무시하고 정확히 일치해야 한다") {
                service.execute(null, "backend", null, null).map { it.id } shouldContainExactly listOf(1L)
                service.execute(null, "back", null, null) shouldBe emptyList()
            }

            it("검색어는 회사명 또는 포지션명에 부분 일치하면 된다") {
                service.execute(" sooner ", null, null, null).map { it.id } shouldContainExactly listOf(2L)
                service.execute("END", null, null, null).map { it.id } shouldContainExactly listOf(1L, 2L)
            }

            it("deadline_asc로 마감일 오름차순 정렬한다") {
                service.execute(null, null, null, "DEADLINE_ASC").map { it.id } shouldContainExactly listOf(2L, 1L)
            }

            it("지원하지 않는 정렬 옵션이면 400을 던진다") {
                val exception = shouldThrow<ExpectedException> { service.execute(null, null, null, "latest") }

                exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    })
