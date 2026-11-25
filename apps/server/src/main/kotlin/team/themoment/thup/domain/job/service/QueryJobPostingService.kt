package team.themoment.thup.domain.job.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.dto.JobPostingDetailResponse
import team.themoment.thup.domain.job.repository.AttachmentRepository
import team.themoment.thup.domain.job.repository.JobPositionRepository
import team.themoment.thup.domain.job.repository.JobPostingRepository

@Service
class QueryJobPostingService(
    private val jobPostingRepository: JobPostingRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val attachmentRepository: AttachmentRepository,
) {
    @Transactional(readOnly = true)
    fun execute(jobId: Long): JobPostingDetailResponse {
        val jobPosting =
            jobPostingRepository.findByIdOrNull(jobId)
                ?: throw ExpectedException("공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val positions = jobPositionRepository.findAllByJobPostingId(jobId)
        val attachments = attachmentRepository.findAllByJobPostingId(jobId)

        return JobPostingDetailResponse.of(jobPosting, positions, attachments)
    }
}
