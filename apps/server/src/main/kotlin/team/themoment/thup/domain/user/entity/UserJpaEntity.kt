package team.themoment.thup.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import team.themoment.thup.domain.user.entity.constant.Role
import java.time.LocalDateTime

@Entity
@Table(name = "tb_user")
class UserJpaEntity(
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: Role,
    @Column(name = "student_id", length = 20)
    var studentId: String? = null,
    @Column(name = "grade")
    var grade: Int? = null,
    @Column(name = "name", nullable = false, length = 50)
    var name: String,
    @Column(name = "email", nullable = false, length = 255, unique = true)
    var email: String,
    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,
    @Column(name = "approved", nullable = false)
    var approved: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Version
    @Column(name = "version", nullable = false)
    var version: Long? = 0L
        protected set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    fun approve() {
        approved = true
    }

    fun promoteToAdmin() {
        role = Role.ADMIN
        approved = false
    }

    fun switchRole(role: Role) {
        this.role = role
        approved = true
    }

    fun updatePhoneNumber(phoneNumber: String?) {
        this.phoneNumber = phoneNumber
    }

    fun updateGrade(grade: Int?) {
        this.grade = grade
    }
}
