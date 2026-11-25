package team.themoment.thup.domain.application.util

import team.themoment.thup.domain.application.entity.ApplicationJpaEntity

object ApplicantResumeFileNameBuilder {
    private val FORBIDDEN_FILE_NAME_CHARS = Regex("[\\\\/:*?\"<>|]")

    // 외부 지원 건(jobPosition 없음)은 이력서가 존재하지 않아 호출부에서 이 메서드에 도달하지 않는다는 전제에 의존한다.
    fun buildLabel(application: ApplicationJpaEntity): String {
        val name = sanitize(application.user.name)
        val studentId = sanitize(application.user.studentId)
        val position = sanitize(application.jobPosition!!.name)

        return "${name}_${studentId}_$position"
    }

    fun buildZipFileName(application: ApplicationJpaEntity): String = buildLabel(application) + ".zip"

    private fun sanitize(value: String?): String = value?.replace(FORBIDDEN_FILE_NAME_CHARS, "") ?: ""
}
