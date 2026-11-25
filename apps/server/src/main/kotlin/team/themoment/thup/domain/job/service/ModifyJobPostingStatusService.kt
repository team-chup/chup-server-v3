package team.themoment.thup.domain.job.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.dto.JobPostingDetailResponse
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository

@Service
class ModifyJobPostingStatusService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val attachmentRepository: AttachmentRepository,
) {
    @Transactional
    fun execute(
        jobId: Long,
        status: JobPostingStatus?,
    ): JobPostingDetailResponse {
        val jobPosting =
            jobPostingRepository.findByIdOrNull(jobId)
                ?: throw ExpectedException("공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        // v2와 동일하게 별도 400 검증 없이, status가 누락되면 서버 에러(500)로 실패한다.
        jobPosting.updateStatus(status!!)

        val positions = jobPositionRepository.findAllByJobPostingId(jobId)
        val attachments = attachmentRepository.findAllByJobPostingId(jobId)
        return JobPostingDetailResponse.of(jobPosting, positions, attachments)
    }
}
