package team.themoment.thup.support

import io.mockk.every
import io.mockk.mockk
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.util.ReflectionTestUtils
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role
import java.time.LocalDate

/** id는 DB가 채우는 값이라 setter가 막혀 있으므로 테스트에서만 리플렉션으로 주입한다. */
fun <T : Any> T.withId(id: Long): T = apply { ReflectionTestUtils.setField(this, "id", id) }

fun oAuth2User(id: Long): OAuth2User =
    mockk {
        every { getAttribute<Any>("id") } returns id
    }

fun user(
    id: Long = 1L,
    role: Role = Role.STUDENT,
    name: String = "홍길동",
    grade: Int? = 3,
): UserJpaEntity =
    UserJpaEntity(
        role = role,
        studentId = "3101",
        grade = grade,
        name = name,
        email = "student$id@gsm.hs.kr",
        approved = true,
    ).withId(id)

fun jobPosting(
    id: Long = 10L,
    companyName: String = "더모먼트",
    employmentType: EmploymentType = EmploymentType.FULL_TIME,
    recruitEnd: LocalDate = LocalDate.now().plusDays(7),
    status: JobPostingStatus = JobPostingStatus.RECRUITING,
): JobPostingJpaEntity =
    JobPostingJpaEntity(
        createdBy = user(id = 99L, role = Role.ADMIN),
        companyName = companyName,
        description = "설명",
        employmentType = employmentType,
        recruitStart = LocalDate.now().minusDays(7),
        recruitEnd = recruitEnd,
        status = status,
    ).withId(id)

fun jobPosition(
    id: Long,
    jobPosting: JobPostingJpaEntity,
    name: String,
): JobPositionJpaEntity = JobPositionJpaEntity(jobPosting = jobPosting, name = name).withId(id)
