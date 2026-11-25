package team.themoment.thup.global.config

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    @field:NotNull
    val allowedOrigins: List<String>,
)
