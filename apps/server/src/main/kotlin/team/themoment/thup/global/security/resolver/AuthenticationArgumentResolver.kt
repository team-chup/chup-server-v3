package team.themoment.thup.global.security.resolver

import org.springframework.core.MethodParameter
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import team.themoment.thup.global.security.annotation.AuthRequest

@Component
class AuthenticationArgumentResolver : HandlerMethodArgumentResolver {
    // Kotlin의 non-null Long 파라미터는 primitive long으로 컴파일되므로 박싱 타입과 함께 허용한다
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthRequest::class.java) &&
            (
                parameter.parameterType == Long::class.javaObjectType ||
                    parameter.parameterType == Long::class.javaPrimitiveType
            )

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is OAuth2AuthenticationToken) {
            val id = authentication.principal.getAttribute<Any>("id")
            if (id is Number) {
                return id.toLong()
            }
        }

        throw AccessDeniedException("인증 정보가 없습니다.")
    }
}
