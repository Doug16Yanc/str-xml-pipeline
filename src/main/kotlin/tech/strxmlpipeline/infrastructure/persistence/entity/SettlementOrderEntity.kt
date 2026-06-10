package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.CharJdbcType
import tech.strxmlpipeline.domain.enum.OrderStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "settlement_order",
    indexes = [
        Index(name = "idx_order_batch_id", columnList = "batch_id"),
        Index(name = "idx_order_status_date", columnList = "status, settlement_date"),
        Index(name = "idx_order_originator", columnList = "originator_id"),
        Index(name = "idx_order_destination", columnList = "destination_id")
    ]
)
class SettlementOrderEntity(
    @Id
    @Column(name = "id", updatable = false)
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "originator_id", nullable = false, updatable = false)
    val originator: ParticipantEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false, updatable = false)
    val destination: ParticipantEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    var batch: FileBatchEntity? = null,

    @Column(name = "order_type", length = 10, nullable = false, updatable = false)
    val orderType: String,

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    val amount: BigDecimal,

    @Column(name = "currency", columnDefinition = "char(3)", length = 3, nullable = false)
    @JdbcType(CharJdbcType::class)
    val currency: String = "BRL",

    @Column(name = "settlement_date", nullable = false)
    val settlementDate: LocalDate,

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,

    @Column(name = "window_code", length = 30, nullable = false, updatable = false)
    val window: String,

    @Column(name = "end_to_end_id", length = 35, nullable = false, updatable = false, unique = true)
    val endToEndId: String,

    @Version
    @Column(name = "version")
    var version: Long = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)