package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.OrderType
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementOrderEntity

fun SettlementOrderEntity.toDomain(): SettlementOrder = SettlementOrder(
    id = this.id,
    type = OrderType.fromCode(this.orderType),
    amount = this.amount,
    settlementDate = this.settlementDate,
    originator = this.originator.toDomain(),
    destination = this.destination.toDomain(),
    endToEndId = "E${this.originator.ispb}${this.settlementDate.toString().replace("-", "")}00000000", // Fallback ou campo mapeado se adicionado à entidade
    status = OrderStatus.valueOf(this.status.name),
    createdAt = this.createdAt,
    updatedAt = this.createdAt
)

fun SettlementOrder.toEntity(batchEntity: FileBatchEntity? = null): SettlementOrderEntity = SettlementOrderEntity(
    id = this.id,
    originator = this.originator.toEntity(),
    destination = this.destination.toEntity(),
    batch = batchEntity,
    orderType = this.type.code,
    amount = this.amount,
    currency = "BRL",
    settlementDate = this.settlementDate,
    status = OrderStatus.valueOf(this.status.name),
    createdAt = this.createdAt
).apply {
    if (batchEntity != null && !batchEntity.orders.contains(this)) {
        batchEntity.orders.add(this)
    }
}