package team.themoment.thup.domain.user.dto

import team.themoment.thup.domain.user.entity.ResumeJpaEntity
import java.time.LocalDateTime

data class ResumeResponse(
    val id: Long?,
    val fileName: String,
    val fileSize: Int,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(resume: ResumeJpaEntity): ResumeResponse =
            ResumeResponse(
                id = resume.id,
                fileName = resume.fileName,
                fileSize = resume.fileSize,
                createdAt = resume.createdAt,
            )
    }
}
