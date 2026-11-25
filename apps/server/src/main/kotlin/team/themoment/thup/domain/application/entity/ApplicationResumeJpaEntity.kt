package team.themoment.thup.domain.application.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "tb_application_resume")
class ApplicationResumeJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: ApplicationJpaEntity,
    @Column(name = "resume_snapshot_url", nullable = false, length = 1000)
    var resumeSnapshotUrl: String,
    @Column(name = "resume_file_name", nullable = false, length = 255)
    var resumeFileName: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set
}
