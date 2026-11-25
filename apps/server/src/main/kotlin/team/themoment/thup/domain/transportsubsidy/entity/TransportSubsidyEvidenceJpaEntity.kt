package team.themoment.thup.domain.transportsubsidy.entity

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
@Table(name = "tb_transport_subsidy_evidence")
class TransportSubsidyEvidenceJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: TransportSubsidyApplicationJpaEntity,
    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String,
    @Column(name = "file_url", nullable = false, length = 1000)
    var fileUrl: String,
    @Column(name = "file_size", nullable = false)
    var fileSize: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set
}
