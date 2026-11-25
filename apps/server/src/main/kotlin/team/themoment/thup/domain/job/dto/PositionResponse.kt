package team.themoment.thup.domain.job.dto

import team.themoment.thup.domain.job.entity.JobPositionJpaEntity

data class PositionResponse(
    val id: Long?,
    val name: String,
) {
    companion object {
        fun from(position: JobPositionJpaEntity): PositionResponse = PositionResponse(position.id, position.name)
    }
}
