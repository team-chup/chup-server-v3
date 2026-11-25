package team.themoment.thup.domain.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import java.time.LocalDateTime

data class AdminApplicantResponse(
    val id: Long?,
    val name: String,
    val studentId: String?,
    val email: String,
    val phoneNumber: String?,
    val companyName: String?,
    val positionName: String?,
    val applicationSource: ApplicationSource,
    val sourcePlatform: String?,
    // is- 접두사 Boolean은 Jackson이 "external"로 직렬화할 수 있어 v2와 같은 키를 명시한다
    @get:JsonProperty("isExternal")
    val isExternal: Boolean,
    val status: ApplicationStatus,
    val appliedAt: LocalDateTime?,
) {
    companion object {
        fun from(application: ApplicationJpaEntity): AdminApplicantResponse {
            val user = application.user
            return AdminApplicantResponse(
                id = application.id,
                name = user.name,
                studentId = user.studentId,
                email = user.email,
                phoneNumber = user.phoneNumber,
                companyName = application.effectiveCompanyName,
                positionName = application.effectivePositionName,
                applicationSource = application.applicationSource,
                sourcePlatform = application.sourcePlatform,
                isExternal = application.isExternal,
                status = application.status,
                appliedAt = application.appliedAt,
            )
        }
    }
}
