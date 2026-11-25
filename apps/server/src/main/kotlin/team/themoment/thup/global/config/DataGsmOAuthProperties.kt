package team.themoment.thup.global.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "oauth.datagsm")
data class DataGsmOAuthProperties(
    @field:NotBlank
    val clientId: String,
    @field:NotBlank
    val clientSecret: String,
    @field:NotBlank
    val redirectUri: String,
    @field:NotEmpty
    val allowedRedirectOrigins: List<String>,
    @field:NotBlank
    val defaultRedirectOrigin: String,
    val successRedirectPath: String?,
    val failureRedirectPath: String?,
) {
    fun isAllowedOrigin(origin: String?): Boolean = origin != null && allowedRedirectOrigins.contains(origin)
}
