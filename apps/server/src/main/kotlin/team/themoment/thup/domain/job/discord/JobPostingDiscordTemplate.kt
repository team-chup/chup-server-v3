package team.themoment.thup.domain.job.discord

import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.global.discord.DiscordEmbed
import java.time.Instant
import java.time.format.DateTimeFormatter

object JobPostingDiscordTemplate {
    private const val COLOR = 0x5865F2
    private const val MAX_DESCRIPTION_LENGTH = 300
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    fun build(
        notification: JobPostingNotification,
        envLabel: String?,
        jobsUrl: String?,
    ): DiscordEmbed =
        DiscordEmbed(
            title = "📢 새로운 채용 공고가 등록되었어요!",
            description = null,
            url = jobsUrl,
            color = COLOR,
            fields =
                listOf(
                    DiscordEmbed.Field.of("🏢 회사명", notification.companyName),
                    DiscordEmbed.Field.of(
                        "📝 회사 설명",
                        DiscordEmbed.truncate(notification.description, MAX_DESCRIPTION_LENGTH),
                    ),
                    DiscordEmbed.Field.of("💼 고용 형태", employmentTypeLabel(notification.employmentType)),
                    DiscordEmbed.Field.of("👩‍💼 모집 포지션", notification.positionNames.joinToString(", ")),
                    DiscordEmbed.Field.of(
                        "📅 모집 기간",
                        DATE_FORMATTER.format(notification.recruitStart) + " ~ " +
                            DATE_FORMATTER.format(notification.recruitEnd),
                    ),
                    DiscordEmbed.Field.of("🔗 링크", "[채용 공고 바로가기]($jobsUrl)"),
                ),
            footer = DiscordEmbed.Footer("[$envLabel] 등록자: ${notification.createdByName}"),
            timestamp = Instant.now().toString(),
        )

    private fun employmentTypeLabel(employmentType: EmploymentType): String =
        when (employmentType) {
            EmploymentType.FULL_TIME -> "정규직"
            EmploymentType.CONTRACT -> "계약직"
            EmploymentType.INTERN -> "인턴"
            EmploymentType.INDUSTRIAL_FUNCTIONAL_PERSONNEL -> "산업기능요원"
        }
}
