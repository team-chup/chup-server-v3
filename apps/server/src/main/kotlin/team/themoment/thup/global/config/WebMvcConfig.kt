package team.themoment.thup.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import team.themoment.thup.global.discord.DiscordErrorAlertInterceptor
import team.themoment.thup.global.security.resolver.AuthenticationArgumentResolver

@Configuration
class WebMvcConfig(
    private val corsProperties: CorsProperties,
    private val authenticationArgumentResolver: AuthenticationArgumentResolver,
    private val discordErrorAlertInterceptor: DiscordErrorAlertInterceptor,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticationArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(discordErrorAlertInterceptor)
    }
}
