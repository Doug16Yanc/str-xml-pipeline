package tech.strxmlpipeline.infrastructure.messaging.message

import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.model.FileBatch

/**
 * Published once a batch enters TRANSMISSION_REJECTED, triggering the
 * CompensatingConsumer (Terceira Milha saga). rejectionReason is carried
 * only for observability/audit — the compensation itself is isonomic
 * (treats every rejection reason the same way, see README).
 */
data class BatchCompensationMessage(
    val batchId: String,
    val windowKey: String,
    val referenceDate: String,
    val rejectionReason: String?,
) {
    companion object {
        fun from(batch: FileBatch, rejectionReason: RejectionReason?) = BatchCompensationMessage(
            batchId         = batch.id.toString(),
            windowKey       = batch.window.partitioningKey,
            referenceDate   = batch.referenceDate.toString(),
            rejectionReason = rejectionReason?.code,
        )
    }
}