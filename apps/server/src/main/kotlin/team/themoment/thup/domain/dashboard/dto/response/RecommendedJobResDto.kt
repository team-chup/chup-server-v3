package team.themoment.thup.domain.dashboard.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendedJobResDto(
    val id: Long?,
    val companyName: String,
    // getter가 getDDay()로 생성되어 Jackson이 "dday"로 직렬화할 수 있어 v2와 같은 키를 명시한다
    @get:JsonProperty("dDay")
    val dDay: Long,
)
