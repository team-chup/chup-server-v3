package team.themoment.thup.domain.auth.service

import org.springframework.stereotype.Service
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient
import team.themoment.thup.domain.auth.dto.AuthorizationUrlResult
import team.themoment.thup.global.config.DataGsmOAuthProperties
import java.util.UUID

@Service
class DataGsmOAuthLoginService(
    private val dataGsmOAuthClient: DataGsmOAuthClient,
    private val dataGsmOAuthProperties: DataGsmOAuthProperties,
) {
    fun execute(requestedRedirectOrigin: String?): AuthorizationUrlResult {
        val state = UUID.randomUUID().toString()
        val redirectOrigin =
            if (dataGsmOAuthProperties.isAllowedOrigin(requestedRedirectOrigin)) {
                requestedRedirectOrigin!!
            } else {
                dataGsmOAuthProperties.defaultRedirectOrigin
            }

        val builder =
            dataGsmOAuthClient
                .createAuthorizationUrl(dataGsmOAuthProperties.redirectUri)
                .state(state)
        return AuthorizationUrlResult(builder.build(), state, redirectOrigin)
    }
}
