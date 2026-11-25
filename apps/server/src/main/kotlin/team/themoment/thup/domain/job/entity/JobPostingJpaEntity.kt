package team.themoment.thup.domain.job.entity

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
import team.themoment.thup.domain.job.entity.constant.EmploymentType
import team.themoment.thup.domain.job.entity.constant.JobPostingStatus
import team.themoment.thup.domain.user.entity.UserJpaEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "tb_job_posting")
class JobPostingJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    var createdBy: UserJpaEntity,
    @Column(name = "company_name", nullable = false, length = 100)
    var companyName: String,
    @Column(name = "description", nullable = false, length = 2000)
    var description: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    var employmentType: EmploymentType,
    @Column(name = "recruit_start", nullable = false)
    var recruitStart: LocalDate,
    @Column(name = "recruit_end", nullable = false)
    var recruitEnd: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: JobPostingStatus = JobPostingStatus.RECRUITING,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    fun update(
        companyName: String,
        description: String,
        employmentType: EmploymentType,
        recruitStart: LocalDate,
        recruitEnd: LocalDate,
    ) {
        this.companyName = companyName
        this.description = description
        this.employmentType = employmentType
        this.recruitStart = recruitStart
        this.recruitEnd = recruitEnd
    }

    fun updateStatus(status: JobPostingStatus) {
        this.status = status
    }
}
