package tech.strxmlpipeline.infrastructure.messaging.message

import tech.strxmlpipeline.domain.model.FileBatch

data class BatchEmissionMessage(
    val batchId: String,
    val windowKey: String,
    val referenceDate: String,
    val totalOrders: Int,
) {
    companion object {
        fun from(batch: FileBatch) = BatchEmissionMessage(
            batchId       = batch.id.toString(),
            windowKey     = batch.window.partitioningKey,
            referenceDate = batch.referenceDate.toString(),
            totalOrders   = batch.totalOrders,
        )
    }
}

