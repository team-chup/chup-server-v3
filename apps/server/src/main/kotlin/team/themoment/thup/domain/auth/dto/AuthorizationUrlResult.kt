package team.themoment.thup.domain.auth.dto

data class AuthorizationUrlResult(
    val url: String,
    val state: String,
    val redirectOrigin: String,
)
