package team.themoment.thup.domain.transportsubsidy.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import team.themoment.thup.domain.transportsubsidy.entity.constant.TransportSubsidyStatus
import team.themoment.thup.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

@Entity
@Table(name = "tb_transport_subsidy_application")
class TransportSubsidyApplicationJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity,
    @Column(name = "company_name", nullable = false, length = 100)
    var companyName: String,
    @Column(name = "interview_at", nullable = false)
    var interviewAt: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TransportSubsidyStatus = TransportSubsidyStatus.PENDING,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @CreationTimestamp
    @Column(name = "applied_at", nullable = false, updatable = false)
    var appliedAt: LocalDateTime? = null
        protected set

    @Column(name = "result_updated_at")
    var resultUpdatedAt: LocalDateTime? = null
        protected set

    fun updateStatus(status: TransportSubsidyStatus) {
        this.status = status
        resultUpdatedAt = LocalDateTime.now()
    }
}
