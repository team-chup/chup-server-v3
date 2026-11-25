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
import team.themoment.thup.domain.job.entity.constant.AttachmentFileType

@Entity
@Table(name = "tb_attachment")
class AttachmentJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    var jobPosting: JobPostingJpaEntity,
    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String,
    @Column(name = "file_url", nullable = false, length = 1000)
    var fileUrl: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    var fileType: AttachmentFileType,
    @Column(name = "file_size", nullable = false)
    var fileSize: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set
}
