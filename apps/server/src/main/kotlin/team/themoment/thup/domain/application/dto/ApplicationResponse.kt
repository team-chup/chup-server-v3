package team.themoment.thup.domain.application.dto

import team.themoment.thup.domain.application.entity.ApplicationJpaEntity
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationResponse(
    val id: Long?,
    val companyName: String?,
    val positionName: String?,
    val applicationSource: ApplicationSource,
    val sourcePlatform: String?,
    val status: ApplicationStatus,
    val appliedAt: LocalDateTime?,
) {
    companion object {
        fun from(application: ApplicationJpaEntity): ApplicationResponse =
            ApplicationResponse(
                id = application.id,
                companyName = application.effectiveCompanyName,
                positionName = application.effectivePositionName,
                applicationSource = application.applicationSource,
                sourcePlatform = application.sourcePlatform,
                status = application.status,
                appliedAt = application.appliedAt,
            )
    }
}
