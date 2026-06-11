package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.OrderType
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementOrderEntity

fun SettlementOrderEntity.toSettlementOrderDomain(): SettlementOrder = SettlementOrder(
    id = this.id,
    type = OrderType.fromCode(this.orderType),
    amount = this.amount,
    settlementDate = this.settlementDate,
    originator = this.originator.toParticipantDomain(),
    destination = this.destination.toParticipantDomain(),
    endToEndId = this.endToEndId,
    status = this.status,
    batchId = this.batch?.id,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun SettlementOrder.toSettlementOrderEntity(
    window: String,
    batchEntity: FileBatchEntity? = null
): SettlementOrderEntity = SettlementOrderEntity(
    id = this.id,
    originator = this.originator.toParticipantEntity(),
    destination = this.destination.toParticipantEntity(),
    batch = batchEntity,
    orderType = this.type.code,
    amount = this.amount,
    currency = "BRL",
    settlementDate = this.settlementDate,
    status = this.status,
    window = window,
    endToEndId = this.endToEndId,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
).apply {
    if (batchEntity != null && !batchEntity.orders.contains(this)) {
        batchEntity.orders.add(this)
    }
}