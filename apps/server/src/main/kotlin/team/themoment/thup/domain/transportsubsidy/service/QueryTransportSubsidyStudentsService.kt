package team.themoment.thup.domain.transportsubsidy.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.thup.domain.transportsubsidy.dto.TransportSubsidyStudentSummaryResponse
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus
import team.themoment.thup.domain.transportsubsidy.repository.TransportSubsidyApplicationRepository
import team.themoment.thup.domain.user.entity.constant.Role
import team.themoment.thup.domain.user.repository.UserRepository

@Service
class QueryTransportSubsidyStudentsService(
    private val userRepository: UserRepository,
    private val applicationRepository: TransportSubsidyApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(): List<TransportSubsidyStudentSummaryResponse> {
        val gradeThreeStudents = userRepository.findAllByRoleAndGrade(Role.STUDENT, TARGET_GRADE)

        val approvedCountByUserId =
            toCountMap(applicationRepository.countGroupedByUserIdAndStatus(TransportSubsidyStatus.APPROVED))
        val totalCountByUserId = toCountMap(applicationRepository.countGroupedByUserId())

        // 신청 이후 역할이 바뀐(예: 관리자로 승격된) 유저도 신청 이력이 있으면 계속 노출되어야 하므로,
        // 3학년 재학생 목록에 신청 이력이 있는 유저를 role/grade와 무관하게 합쳐서 보여준다.
        val gradeThreeStudentIds = gradeThreeStudents.map { it.id }.toSet()
        val applicantsOutsideGradeThree = totalCountByUserId.keys.filter { it !in gradeThreeStudentIds }
        val otherApplicants =
            if (applicantsOutsideGradeThree.isEmpty()) {
                emptyList()
            } else {
                userRepository.findAllById(applicantsOutsideGradeThree)
            }

        return (gradeThreeStudents + otherApplicants).map { student ->
            TransportSubsidyStudentSummaryResponse(
                userId = student.id,
                name = student.name,
                studentId = student.studentId,
                approvedCount = approvedCountByUserId[student.id] ?: 0L,
                totalCount = totalCountByUserId[student.id] ?: 0L,
            )
        }
    }

    private fun toCountMap(rows: List<Array<Any>>): Map<Long, Long> = rows.associate { row -> row[0] as Long to row[1] as Long }

    companion object {
        private const val TARGET_GRADE = 3
    }
}
