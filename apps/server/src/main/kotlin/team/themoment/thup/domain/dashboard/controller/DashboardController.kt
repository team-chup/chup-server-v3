package team.themoment.thup.domain.dashboard.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.thup.domain.dashboard.dto.response.AdminDashboardResDto
import team.themoment.thup.domain.dashboard.dto.response.StudentDashboardResDto
import team.themoment.thup.domain.dashboard.service.QueryAdminDashboardService
import team.themoment.thup.domain.dashboard.service.QueryStudentDashboardService

@RestController
@Tag(name = "Dashboard", description = "대시보드 API")
class DashboardController(
    private val queryStudentDashboardService: QueryStudentDashboardService,
    private val queryAdminDashboardService: QueryAdminDashboardService,
) {
    @Operation(summary = "학생 홈 대시보드", description = "모집중 공고 수, 나의 지원 건수, 서류 합격 건수, 추천 공고를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    )
    @GetMapping("/api/dashboard")
    fun queryStudentDashboard(
        @AuthenticationPrincipal user: OAuth2User,
    ): StudentDashboardResDto = queryStudentDashboardService.execute(user)

    @Operation(summary = "관리자 대시보드", description = "모집중 공고, 전체 지원자, 결과 대기, 서류 합격 건수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
    )
    @GetMapping("/api/admin/dashboard")
    fun queryAdminDashboard(): AdminDashboardResDto = queryAdminDashboardService.execute()
}
