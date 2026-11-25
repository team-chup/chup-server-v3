package team.themoment.thup.global.discord

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.Instant

internal object ErrorAlertTemplate {
    private const val COLOR_SERVER_ERROR = 0xE74C3C
    private const val COLOR_CLIENT_ERROR = 0xE67E22
    private const val MAX_STACK_TRACE_FRAMES = 12
    private const val MAX_STACK_TRACE_LENGTH = 1000

    fun build(
        envLabel: String?,
        status: Int,
        request: HttpServletRequest,
        exception: Exception?,
        suppressedCount: Int,
    ): DiscordEmbed {
        val fields = mutableListOf<DiscordEmbed.Field>()
        fields.add(DiscordEmbed.Field.of("🌐 요청", request.method + " " + fullPath(request)))
        fields.add(DiscordEmbed.Field.of("👤 사용자", resolveUser()))

        if (exception != null) {
            fields.add(
                DiscordEmbed.Field.of("💥 예외", exception.javaClass.simpleName + ": " + exception.message),
            )
            // 코드블록 마크업까지 포함해 1024자를 넘기지 않도록 본문을 먼저 줄인다
            fields.add(
                DiscordEmbed.Field.of(
                    "📄 스택트레이스",
                    "```" + DiscordEmbed.truncate(stackTrace(exception), MAX_STACK_TRACE_LENGTH) + "```",
                ),
            )
        }
        if (suppressedCount > 0) {
            fields.add(DiscordEmbed.Field.of("🔁 반복", "직전 발송 이후 동일 에러가 ${suppressedCount}회 더 발생했습니다."))
        }

        return DiscordEmbed(
            title = "[$envLabel] 🚨 $status ${reasonPhrase(status)}",
            description = null,
            url = null,
            color = if (status >= 500) COLOR_SERVER_ERROR else COLOR_CLIENT_ERROR,
            fields = fields,
            footer = null,
            timestamp = Instant.now().toString(),
        )
    }

    private fun fullPath(request: HttpServletRequest): String {
        val queryString = request.queryString
        return if (queryString == null) request.requestURI else request.requestURI + "?" + queryString
    }

    private fun resolveUser(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? OAuth2User ?: return "비로그인"
        return "${user.getAttribute<Any>("email")} (${user.getAttribute<Any>("role")})"
    }

    private fun stackTrace(exception: Exception): String =
        exception.stackTrace
            .take(MAX_STACK_TRACE_FRAMES)
            .joinToString(separator = "") { "$it\n" }

    private fun reasonPhrase(status: Int): String = HttpStatus.resolve(status)?.reasonPhrase ?: ""
}
