package team.themoment.thup.domain.application.entity

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
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.job.entity.JobPositionJpaEntity
import team.themoment.thup.domain.job.entity.JobPostingJpaEntity
import team.themoment.thup.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

@Entity
@Table(
    name = "tb_application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "job_posting_id"])],
)
class ApplicationJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    var jobPosting: JobPostingJpaEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id")
    var jobPosition: JobPositionJpaEntity? = null,
    @Column(name = "company_name", length = 100)
    var companyName: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(
        name = "application_source",
        nullable = false,
        columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'OFFICIAL'",
    )
    var applicationSource: ApplicationSource = ApplicationSource.OFFICIAL,
    // 외부 지원 건의 플랫폼명 (잡코리아, 원티드 등) 자유 입력
    @Column(name = "source_platform", length = 100)
    var sourcePlatform: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ApplicationStatus = ApplicationStatus.APPLIED,
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

    val effectiveCompanyName: String?
        get() = jobPosting?.companyName ?: companyName

    val effectivePositionName: String?
        get() = jobPosition?.name

    val isExternal: Boolean
        get() = applicationSource != ApplicationSource.OFFICIAL

    fun updateStatus(status: ApplicationStatus) {
        this.status = status
        resultUpdatedAt = LocalDateTime.now()
    }
}
