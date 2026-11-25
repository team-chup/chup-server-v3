package team.themoment.thup.global.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "session.cookie")
data class SessionCookieProperties(
    val secure: Boolean = false,
    @field:NotBlank
    val sameSite: String,
    val domain: String?,
) {
    fun hasDomain(): Boolean = !domain.isNullOrBlank()
}
