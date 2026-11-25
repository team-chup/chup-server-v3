package team.themoment.thup.global.config

import jakarta.servlet.DispatcherType
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.http.SessionRepositoryFilter
import java.util.EnumSet

@Configuration
@EnableRedisHttpSession
class HttpSessionConfig {
    @Bean
    fun springSessionRepositoryFilterRegistration(
        springSessionRepositoryFilter: SessionRepositoryFilter<*>,
    ): FilterRegistrationBean<SessionRepositoryFilter<*>> {
        val registration = FilterRegistrationBean<SessionRepositoryFilter<*>>(springSessionRepositoryFilter)
        registration.order = SESSION_FILTER_ORDER
        registration.setDispatcherTypes(
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC),
        )
        return registration
    }

    companion object {
        private const val SESSION_FILTER_ORDER = Int.MIN_VALUE + 50
    }
}
