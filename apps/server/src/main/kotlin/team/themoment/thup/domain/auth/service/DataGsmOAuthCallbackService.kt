package team.themoment.thup.domain.auth.service

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient
import team.themoment.datagsm.sdk.oauth.exception.DataGsmException
import team.themoment.datagsm.sdk.oauth.model.UserInfo
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository
import team.themoment.thup.global.config.DataGsmOAuthProperties

@Service
class DataGsmOAuthCallbackService(
    private val dataGsmOAuthClient: DataGsmOAuthClient,
    private val dataGsmOAuthProperties: DataGsmOAuthProperties,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        code: String,
        state: String?,
        stateCookie: String?,
        redirectOriginCookie: String?,
        request: HttpServletRequest,
    ): String {
        val redirectOrigin =
            if (dataGsmOAuthProperties.isAllowedOrigin(redirectOriginCookie)) {
                redirectOriginCookie
            } else {
                dataGsmOAuthProperties.defaultRedirectOrigin
            }

        return try {
            if (state == null || state != stateCookie) {
                throw ExpectedException("유효하지 않은 state 값입니다.", HttpStatus.BAD_REQUEST)
            }

            val tokenResponse = dataGsmOAuthClient.exchangeCodeForToken(code, dataGsmOAuthProperties.redirectUri)
            val userInfo = dataGsmOAuthClient.getUserInfo(tokenResponse.accessToken)

            if (!userInfo.isStudent() || userInfo.student == null) {
                throw ExpectedException("학생만 로그인할 수 있습니다.", HttpStatus.FORBIDDEN)
            }

            val user = getOrCreateUser(userInfo)
            authenticate(user, request)

            redirectOrigin + dataGsmOAuthProperties.successRedirectPath
        } catch (e: DataGsmException) {
            log.warn("Failed DataGSM OAuth login. reason={}", e.message)
            redirectOrigin + dataGsmOAuthProperties.failureRedirectPath
        } catch (e: ExpectedException) {
            log.warn("Failed DataGSM OAuth login. reason={}", e.message)
            redirectOrigin + dataGsmOAuthProperties.failureRedirectPath
        }
    }

    private fun getOrCreateUser(userInfo: UserInfo): UserJpaEntity {
        val student = userInfo.student
        val existing = userRepository.findByEmail(userInfo.email)
        if (existing != null) {
            // 진급 등으로 학년이 바뀔 수 있어 로그인할 때마다 최신 값으로 갱신한다
            existing.updateGrade(student.grade)
            return existing
        }
        return userRepository.save(
            UserJpaEntity(
                email = userInfo.email,
                name = student.name,
                studentId = student.studentNumber.toString(),
                grade = student.grade,
                role = Role.STUDENT,
                approved = true,
            ),
        )
    }

    private fun authenticate(
        user: UserJpaEntity,
        request: HttpServletRequest,
    ) {
        // 관리자로 지정만 되고 아직 승인되지 않은 계정은 승인 전까지 관리자 권한을 주지 않는다
        val effectiveRole = if (user.role == Role.ADMIN && !user.approved) Role.STUDENT else user.role

        val oAuth2User =
            DefaultOAuth2User(
                listOf(SimpleGrantedAuthority(effectiveRole.authority)),
                mapOf(
                    "id" to user.id!!,
                    "role" to user.role.name,
                    "email" to user.email,
                    "name" to user.name,
                ),
                "id",
            )

        val authentication =
            OAuth2AuthenticationToken(
                oAuth2User,
                oAuth2User.authorities,
                "datagsm",
            )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)

        request.getSession(false)?.invalidate()
        val session = request.getSession(true)
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DataGsmOAuthCallbackService::class.java)
    }
}
