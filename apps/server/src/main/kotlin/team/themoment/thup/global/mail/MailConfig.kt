package team.themoment.thup.global.mail

import com.resend.Resend
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MailConfig(
    private val mailProperties: MailProperties,
) {
    @Bean
    fun resend(): Resend = Resend(mailProperties.apiKey)
}
