package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.enum.ReturnResult
import java.time.OffsetDateTime
import java.util.UUID

data class SettlementReturn(
    val id: UUID = UUID.randomUUID(),
    val batchId: UUID,
    val fileId: UUID,
    val result: ReturnResult,
    val messageCode: String,
    val description: String,
    val rejectionReason: RejectionReason? = null,
    val receivedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    init {
        require(messageCode.isNotBlank()) { "Message code must not be blank" }
        if (result == ReturnResult.REJECTED) {
            requireNotNull(rejectionReason) {
                "Rejection reason is mandatory when result is REJECTED"
            }
        }
    }

    val isAccepted: Boolean get() = result == ReturnResult.ACCEPTED
    val isRejected: Boolean get() = result == ReturnResult.REJECTED
}
