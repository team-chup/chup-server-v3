package team.themoment.thup.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import team.themoment.thup.domain.user.entity.constant.Role

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val sessionCookieProperties: SessionCookieProperties,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            .formLogin(AbstractHttpConfigurer<*, *>::disable)
            .httpBasic(AbstractHttpConfigurer<*, *>::disable)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }.securityContext { secContext ->
                secContext.securityContextRepository(securityContextRepository())
            }.authorizeHttpRequests { auth ->
                auth
                    // 개발용 역할 전환 API: ROLE_ADMIN이 없는 계정도 스스로 전환할 수 있어야 하므로 인증만 요구.
                    // 실제 노출 여부는 DevToolsProperties.enabled(prod에서는 항상 false)가 통제한다.
                    .requestMatchers(HttpMethod.PATCH, "/api/admin/users/*/dev-role")
                    .authenticated()
                    .requestMatchers("/api/admin/**")
                    .hasAuthority(Role.ADMIN.authority)
                    .requestMatchers(*PUBLIC_PATHS)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        serializer.setCookieName("SESSION")
        serializer.setUseHttpOnlyCookie(true)
        serializer.setSameSite(sessionCookieProperties.sameSite)
        serializer.setUseSecureCookie(sessionCookieProperties.secure)
        serializer.setCookiePath("/")
        sessionCookieProperties.domain
            ?.takeIf { sessionCookieProperties.hasDomain() }
            ?.let { serializer.setDomainName(it) }
        return serializer
    }

    companion object {
        private val PUBLIC_PATHS =
            arrayOf(
                "/api/auth/datagsm/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/error",
            )
    }
}
