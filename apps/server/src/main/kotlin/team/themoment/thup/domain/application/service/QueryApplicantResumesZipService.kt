package team.themoment.thup.domain.application.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.repository.ApplicationRepository
import team.themoment.thup.domain.application.repository.ApplicationResumeRepository
import team.themoment.thup.domain.application.util.ApplicantResumeFileNameBuilder
import team.themoment.thup.global.storage.FileDownload
import team.themoment.thup.global.storage.FileStorageService
import team.themoment.thup.global.storage.ZipBuilder

@Service
class QueryApplicantResumesZipService(
    private val applicationRepository: ApplicationRepository,
    private val applicationResumeRepository: ApplicationResumeRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional(readOnly = true)
    fun execute(companyId: Long?): FileDownload {
        val applications =
            if (companyId == null) {
                applicationRepository.findAllWithDetails()
            } else {
                applicationRepository.findAllWithDetailsByJobPostingId(companyId)
            }

        if (applications.isEmpty()) {
            throw ExpectedException("지원자가 없습니다.", HttpStatus.NOT_FOUND)
        }

        val zipBytes = ZipBuilder.build(buildEntries(applications), fileStorageService)
        val zipFileName =
            if (companyId == null) {
                "전체.zip"
            } else {
                applications[0].jobPosting!!.companyName + ".zip"
            }

        return FileDownload(ByteArrayResource(zipBytes), zipFileName)
    }

    private fun buildEntries(applications: List<ApplicationJpaEntity>): List<ZipBuilder.Entry> {
        // 외부 지원 건은 이력서 업로드 경로가 없고 jobPosition도 없어 파일명 라벨을 만들 수 없으므로 제외한다.
        val officialApplications = applications.filter { !it.isExternal }

        val applicationIds = officialApplications.map { it.id!! }
        val resumesByApplicationId =
            applicationResumeRepository
                .findAllByApplicationIdIn(applicationIds)
                .groupBy { it.application.id }

        val usedFolderNames = mutableMapOf<String, Int>()
        val entries = mutableListOf<ZipBuilder.Entry>()

        for (application in officialApplications) {
            val folderName =
                ZipBuilder.uniqueName(ApplicantResumeFileNameBuilder.buildLabel(application), usedFolderNames)
            val resumes = resumesByApplicationId[application.id].orEmpty()

            val usedFileNames = mutableMapOf<String, Int>()
            for (resume in resumes) {
                val fileName = ZipBuilder.uniqueName(resume.resumeFileName, usedFileNames)
                entries.add(ZipBuilder.Entry("$folderName/$fileName", resume.resumeSnapshotUrl))
            }
        }

        return entries
    }
}
