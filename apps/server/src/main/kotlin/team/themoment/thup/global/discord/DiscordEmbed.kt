package team.themoment.thup.global.discord

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 디스코드 웹훅 embed 페이로드.
 * 길이 제한(description 4096자, field value 1024자)을 넘기면 디스코드가 400을 반환하므로 절삭해서 담는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordEmbed(
    val title: String?,
    val description: String?,
    val url: String?,
    val color: Int?,
    val fields: List<Field>?,
    val footer: Footer?,
    val timestamp: String?,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Field(
        val name: String?,
        val value: String?,
        val inline: Boolean,
    ) {
        companion object {
            fun of(
                name: String?,
                value: String?,
            ): Field = Field(name, truncate(value, MAX_FIELD_VALUE_LENGTH), false)
        }
    }

    data class Footer(
        val text: String?,
    )

    companion object {
        private const val MAX_FIELD_VALUE_LENGTH = 1024

        fun truncate(
            value: String?,
            maxLength: Int,
        ): String? {
            if (value == null || value.length <= maxLength) {
                return value
            }
            return value.substring(0, maxLength - 3) + "..."
        }
    }
}
