package team.themoment.thup.domain.application.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.application.repository.ApplicationResumeRepository
import team.themoment.thup.domain.application.util.ApplicantResumeFileNameBuilder
import team.themoment.thup.global.storage.FileDownload
import team.themoment.thup.global.storage.FileStorageService
import team.themoment.thup.global.storage.ZipBuilder

@Service
class QueryApplicantResumeService(
    private val applicationRepository: ApplicationRepository,
    private val applicationResumeRepository: ApplicationResumeRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional(readOnly = true)
    fun execute(applicationId: Long): FileDownload {
        val application =
            applicationRepository.findByIdOrNull(applicationId)
                ?: throw ExpectedException("지원 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val resumes = applicationResumeRepository.findAllByApplicationId(applicationId)
        if (resumes.isEmpty()) {
            throw ExpectedException("이력서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val usedNames = mutableMapOf<String, Int>()
        val entries =
            resumes.map { resume ->
                ZipBuilder.Entry(
                    ZipBuilder.uniqueName(resume.resumeFileName, usedNames),
                    resume.resumeSnapshotUrl,
                )
            }

        val zipBytes = ZipBuilder.build(entries, fileStorageService)
        return FileDownload(ByteArrayResource(zipBytes), ApplicantResumeFileNameBuilder.buildZipFileName(application))
    }
}
