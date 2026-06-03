package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity

fun FileBatchEntity.toDomain(): FileBatch = FileBatch(
    id = this.id,
    window = SettlementWindow.parse(this.window),
    referenceDate = this.generatedAt.toLocalDate(),
    status = BatchStatus.valueOf(this.status.name),
    orders = this.orders.map { it.toDomain() },
    createdAt = this.generatedAt,
    updatedAt = this.sentAt ?: this.generatedAt
)

fun FileBatch.toEntity(): FileBatchEntity {
    val batchEntity = FileBatchEntity(
        id = this.id,
        participant = this.orders.first().originator.toEntity(),
        window = this.window.partitioningKey,
        status = BatchStatus.valueOf(this.status.name),
        totalOrders = this.totalOrders,
        totalAmount = this.totalAmount,
        generatedAt = this.createdAt,
        sentAt = this.updatedAt
    )
    this.orders.forEach { it.toEntity(batchEntity) }
    return batchEntity
}