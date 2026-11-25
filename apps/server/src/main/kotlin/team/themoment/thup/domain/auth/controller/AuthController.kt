package team.themoment.thup.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.thup.domain.auth.dto.response.MyAuthInfoResDto
import team.themoment.thup.domain.auth.service.DataGsmOAuthCallbackService
import team.themoment.thup.domain.auth.service.DataGsmOAuthLoginService
import team.themoment.thup.domain.auth.service.LogoutService
import team.themoment.thup.domain.auth.service.QueryMyAuthInfoService
import team.themoment.thup.global.security.annotation.AuthRequest

@RestController
@Tag(name = "Auth", description = "인증 API")
@RequestMapping("/api/auth")
class AuthController(
    private val dataGsmOAuthLoginService: DataGsmOAuthLoginService,
    private val dataGsmOAuthCallbackService: DataGsmOAuthCallbackService,
    private val queryMyAuthInfoService: QueryMyAuthInfoService,
    private val logoutService: LogoutService,
) {
    @Operation(
        summary = "DataGSM OAuth 로그인 시작",
        description =
            "DataGSM 로그인 페이지로 리다이렉트합니다. redirectOrigin으로 로그인 완료 후 돌아갈 프론트 origin을 지정할 수 있습니다" +
                "(허용 목록에 없으면 기본값 사용).",
    )
    @ApiResponses(
        ApiResponse(responseCode = "302", description = "DataGSM 인가 페이지로 리다이렉트"),
    )
    @GetMapping("/datagsm/login")
    fun login(
        @RequestParam(required = false) redirectOrigin: String?,
        response: HttpServletResponse,
    ) {
        val result = dataGsmOAuthLoginService.execute(redirectOrigin)

        response.addCookie(callbackCookie(STATE_COOKIE_NAME, result.state))
        response.addCookie(callbackCookie(REDIRECT_ORIGIN_COOKIE_NAME, result.redirectOrigin))

        response.sendRedirect(result.url)
    }

    @Operation(summary = "DataGSM OAuth 콜백", description = "code로 토큰을 교환하고, 최초 로그인 시 자동 회원가입 후 세션을 발급합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "302", description = "로그인 성공/실패 리다이렉트"),
    )
    @GetMapping("/datagsm/callback")
    fun callback(
        @RequestParam code: String,
        @RequestParam state: String,
        @CookieValue(name = STATE_COOKIE_NAME, required = false) stateCookie: String?,
        @CookieValue(name = REDIRECT_ORIGIN_COOKIE_NAME, required = false) redirectOriginCookie: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val redirectUrl = dataGsmOAuthCallbackService.execute(code, state, stateCookie, redirectOriginCookie, request)
        response.sendRedirect(redirectUrl)
    }

    @Operation(summary = "내 계정 정보 조회", description = "로그인한 사용자의 role 및 관리자 승인 여부를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
    )
    @GetMapping("/me")
    fun me(
        @AuthRequest userId: Long,
    ): MyAuthInfoResDto = queryMyAuthInfoService.execute(userId)

    @Operation(summary = "로그아웃", description = "현재 세션을 무효화합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그아웃 성공"),
    )
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        logoutService.execute(request)
    }

    private fun callbackCookie(
        name: String,
        value: String,
    ): Cookie {
        val cookie = Cookie(name, value)
        cookie.isHttpOnly = true
        cookie.path = CALLBACK_PATH
        cookie.maxAge = STATE_COOKIE_MAX_AGE_SECONDS
        return cookie
    }

    companion object {
        private const val STATE_COOKIE_NAME = "datagsm_oauth_state"
        private const val REDIRECT_ORIGIN_COOKIE_NAME = "datagsm_oauth_redirect_origin"
        private const val STATE_COOKIE_MAX_AGE_SECONDS = 300
        private const val CALLBACK_PATH = "/api/auth/datagsm/callback"
    }
}
