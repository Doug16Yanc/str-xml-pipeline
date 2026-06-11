package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity


fun FileBatchEntity.toFileBatchDomain(): FileBatch {
    if (this.orders.isEmpty()) {
        return FileBatch.fromPersistence(
            id            = this.id,
            window        = SettlementWindow.parse(this.window),
            referenceDate = this.referenceDate,
            totalOrders   = this.totalOrders,
            status        = this.status,
            participant   = this.participant.toParticipantDomain(),
            createdAt     = this.generatedAt,
            updatedAt     = this.sentAt ?: this.generatedAt
        )
    }

    return FileBatch(
        id                  = this.id,
        window              = SettlementWindow.parse(this.window),
        referenceDate       = this.referenceDate,
        orders              = this.orders.map { it.toSettlementOrderDomain() },
        totalOrdersOverride = this.totalOrders,
        status              = this.status,
        participant         = this.participant.toParticipantDomain(),
        createdAt           = this.generatedAt,
        updatedAt           = this.sentAt ?: this.generatedAt
    )
}

fun FileBatch.toFileBatchEntity(): FileBatchEntity {
    val batchEntity = FileBatchEntity(
        id = this.id,
        participant = this.participant.toParticipantEntity(),
        window = this.window.partitioningKey,
        referenceDate = this.referenceDate,
        status = this.status,
        totalOrders = this.totalOrders,
        totalAmount = this.totalAmount,
        generatedAt = this.createdAt,
        sentAt = if (this.updatedAt != this.createdAt) { this.updatedAt } else { null }
    )

    this.orders.forEach { order ->
        order.toSettlementOrderEntity(
            window = this.window.partitioningKey,
            batchEntity = batchEntity
        )
    }

    return batchEntity
}