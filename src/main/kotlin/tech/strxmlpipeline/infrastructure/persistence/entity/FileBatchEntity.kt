package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import tech.strxmlpipeline.domain.enum.BatchStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "file_batch",
    indexes = [
        Index(name = "idx_file_batch_window_status", columnList = "window_code, status"),
        Index(name = "idx_file_batch_window_date",   columnList = "window_code, reference_date"),
        Index(name = "idx_file_batch_participant",   columnList = "participant_id"),
        Index(name = "idx_file_batch_window_date_participant", columnList = "window_code, reference_date, participant_id"),    ]
)
class FileBatchEntity(
    @Id
    @Column(name = "id", updatable = false)
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false, updatable = false)
    val participant: ParticipantEntity,

    @Column(name = "window_code", length = 30, nullable = false, updatable = false)
    val window: String,

    @Column(name = "reference_date", nullable = false, updatable = false)
    val referenceDate: LocalDate,

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: BatchStatus,

    @Column(name = "total_orders", nullable = false)
    var totalOrders: Int = 0,

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "sent_at")
    var sentAt: OffsetDateTime? = null,
) {
    @OneToMany(mappedBy = "batch", fetch = FetchType.LAZY)
    val orders: MutableList<SettlementOrderEntity> = mutableListOf()
}