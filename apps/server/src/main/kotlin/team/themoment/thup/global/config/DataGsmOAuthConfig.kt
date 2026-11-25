package team.themoment.thup.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient

@Configuration
class DataGsmOAuthConfig(
    private val dataGsmOAuthProperties: DataGsmOAuthProperties,
) {
    @Bean
    fun dataGsmOAuthClient(): DataGsmOAuthClient =
        DataGsmOAuthClient
            .builder(
                dataGsmOAuthProperties.clientId,
                dataGsmOAuthProperties.clientSecret,
            ).build()
}
