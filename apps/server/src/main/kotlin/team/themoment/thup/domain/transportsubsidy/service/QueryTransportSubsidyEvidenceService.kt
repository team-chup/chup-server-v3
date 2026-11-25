package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyEvidenceRepository
import team.themoment.thup.global.storage.FileDownload
import team.themoment.thup.global.storage.FileStorageService
import team.themoment.thup.global.storage.ZipBuilder

@Service
class QueryTransportSubsidyEvidenceService(
    private val applicationRepository: TransportSubsidyApplicationRepository,
    private val evidenceRepository: TransportSubsidyEvidenceRepository,
    private val fileStorageService: FileStorageService,
) {
    @Transactional(readOnly = true)
    fun execute(applicationId: Long): FileDownload {
        val application =
            applicationRepository.findByIdOrNull(applicationId)
                ?: throw ExpectedException("신청 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val evidences = evidenceRepository.findAllByApplicationId(applicationId)
        if (evidences.isEmpty()) {
            throw ExpectedException("증빙 서류를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val usedNames = mutableMapOf<String, Int>()
        val entries =
            evidences.map { evidence ->
                ZipBuilder.Entry(ZipBuilder.uniqueName(evidence.fileName, usedNames), evidence.fileUrl)
            }

        val zipBytes = ZipBuilder.build(entries, fileStorageService)
        val zipFileName = "${sanitize(application.user.name)}_${sanitize(application.companyName)}_증빙서류.zip"

        return FileDownload(ByteArrayResource(zipBytes), zipFileName)
    }

    private fun sanitize(value: String?): String = value?.replace(FORBIDDEN_FILE_NAME_CHARS, "") ?: ""

    companion object {
        private val FORBIDDEN_FILE_NAME_CHARS = Regex("[\\\\/:*?\"<>|]")
    }
}
