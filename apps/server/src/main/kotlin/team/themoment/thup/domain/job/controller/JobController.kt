package team.themoment.thup.domain.job.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.themoment.thup.domain.job.dto.AdminJobPostingResponse
import team.themoment.thup.domain.job.dto.JobPostingDetailResponse
import team.themoment.thup.domain.job.dto.JobPostingSummaryResponse
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.service.CreateJobPostingService
import team.themoment.thup.domain.job.service.ModifyJobPostingService
import team.themoment.thup.domain.job.service.ModifyJobPostingStatusService
import team.themoment.thup.domain.job.service.QueryAdminJobPostingsService
import team.themoment.thup.domain.job.service.QueryAttachmentService
import team.themoment.thup.domain.job.service.QueryJobPostingService
import team.themoment.thup.domain.job.service.QueryJobPostingsService
import team.themoment.thup.global.storage.FileDownloadResponses
import java.time.LocalDate

@RestController
@Tag(name = "Job", description = "채용공고 API")
class JobController(
    private val queryJobPostingsService: QueryJobPostingsService,
    private val queryJobPostingService: QueryJobPostingService,
    private val queryAttachmentService: QueryAttachmentService,
    private val queryAdminJobPostingsService: QueryAdminJobPostingsService,
    private val createJobPostingService: CreateJobPostingService,
    private val modifyJobPostingService: ModifyJobPostingService,
    private val modifyJobPostingStatusService: ModifyJobPostingStatusService,
) {
    @Operation(
        summary = "채용 공고 목록",
        description = "모집중인 채용 공고 목록을 검색/필터/정렬하여 조회합니다. 공고별 모집 포지션 목록을 함께 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    )
    @GetMapping("/api/jobs")
    fun queryJobPostings(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) position: String?,
        @RequestParam(required = false) employmentType: EmploymentType?,
        @RequestParam(required = false) sort: String?,
    ): List<JobPostingSummaryResponse> = queryJobPostingsService.execute(q, position, employmentType, sort)

    @Operation(
        summary = "채용 공고 상세",
        description = "채용 공고 상세 정보를 조회합니다. 포지션 목록(id, name)과 첨부파일 목록(id, fileName)을 함께 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음"),
    )
    @GetMapping("/api/jobs/{jobId}")
    fun queryJobPosting(
        @PathVariable jobId: Long,
    ): JobPostingDetailResponse = queryJobPostingService.execute(jobId)

    @Operation(summary = "공고 첨부파일 다운로드", description = "채용 공고에 첨부된 파일을 다운로드합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "다운로드 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "404", description = "첨부파일을 찾을 수 없음"),
    )
    @GetMapping("/api/jobs/{jobId}/attachments/{fileId}")
    fun downloadAttachment(
        @PathVariable jobId: Long,
        @PathVariable fileId: Long,
    ): ResponseEntity<Resource> = FileDownloadResponses.of(queryAttachmentService.execute(jobId, fileId))

    @Operation(
        summary = "관리자 공고 목록",
        description = "상태와 무관하게 전체 채용 공고 목록을 회사명으로 검색하여 조회합니다. 공고별 지원자 수를 함께 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
    )
    @GetMapping("/api/admin/jobs")
    fun queryAdminJobPostings(
        @RequestParam(required = false) q: String?,
    ): List<AdminJobPostingResponse> = queryAdminJobPostingsService.execute(q)

    @Operation(summary = "공고 등록", description = "채용 공고와 포지션, 첨부파일(최대 5개, PDF/HWP/HWPX/이미지)을 등록합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "등록 성공"),
        ApiResponse(responseCode = "400", description = "필수값 누락 또는 지원하지 않는 첨부파일 형식"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ApiResponse(responseCode = "413", description = "첨부파일 개수·용량 초과"),
    )
    @PostMapping(value = ["/api/admin/jobs"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createJobPosting(
        @AuthenticationPrincipal admin: OAuth2User,
        @RequestParam companyName: String,
        @RequestParam description: String,
        @RequestParam employmentType: EmploymentType,
        @RequestParam recruitStart: LocalDate,
        @RequestParam recruitEnd: LocalDate,
        @RequestParam positionNames: List<String>,
        @RequestParam(required = false) attachments: List<MultipartFile>?,
    ): JobPostingDetailResponse =
        createJobPostingService.execute(
            admin,
            companyName,
            description,
            employmentType,
            recruitStart,
            recruitEnd,
            positionNames,
            attachments,
        )

    @Operation(
        summary = "공고 수정",
        description =
            "채용 공고 정보를 수정합니다. positionNames를 보내면 포지션 목록을 동기화하고(신규 추가/미포함분 삭제), " +
                "지원자가 있는 포지션은 삭제하지 않고 409를 반환합니다. " +
                "첨부파일은 retainedAttachmentIds(유지할 기존 첨부파일 id 목록)와 attachments(새로 추가할 파일)로 수정합니다. " +
                "둘 다 안 보내면: 변경 없음. retainedAttachmentIds만 보내면: 그 목록에 없는 기존 파일 삭제(추가 없음). " +
                "attachments만 보내면(retainedAttachmentIds 생략): 기존 파일은 그대로 유지하고 새 파일만 추가. " +
                "retainedAttachmentIds와 attachments를 함께 보내면: 목록에 없는 기존 파일은 삭제하고 새 파일 추가. " +
                "retainedAttachmentIds=[](빈 배열)을 보내면: 기존 파일 전부 삭제. 유지+추가 합계가 5개를 넘으면 413을 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "지원하지 않는 첨부파일 형식"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음"),
        ApiResponse(responseCode = "409", description = "지원자가 있는 포지션을 삭제하려 함"),
        ApiResponse(responseCode = "413", description = "첨부파일 개수·용량 초과"),
    )
    @PatchMapping(value = ["/api/admin/jobs/{jobId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun modifyJobPosting(
        @PathVariable jobId: Long,
        @RequestParam companyName: String,
        @RequestParam description: String,
        @RequestParam employmentType: EmploymentType,
        @RequestParam recruitStart: LocalDate,
        @RequestParam recruitEnd: LocalDate,
        @RequestParam(required = false) positionNames: List<String>?,
        @RequestParam(required = false) retainedAttachmentIds: List<Long>?,
        @RequestParam(required = false) attachments: List<MultipartFile>?,
    ): JobPostingDetailResponse =
        modifyJobPostingService.execute(
            jobId,
            companyName,
            description,
            employmentType,
            recruitStart,
            recruitEnd,
            positionNames,
            retainedAttachmentIds,
            attachments,
        )

    @Operation(summary = "공고 상태 변경", description = "채용 공고의 모집 상태를 변경합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음"),
    )
    @PatchMapping("/api/admin/jobs/{jobId}/status")
    fun modifyJobPostingStatus(
        @PathVariable jobId: Long,
        @RequestBody request: JobPostingStatusRequest,
    ): JobPostingDetailResponse = modifyJobPostingStatusService.execute(jobId, request.status)

    data class JobPostingStatusRequest(
        val status: JobPostingStatus?,
    )
}
