package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.enum.ResponseResult
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Inbound protocol from BACEN/STR confirming the acceptance or rejection of a file batch.
 * Immutable by design: the response is a settled fact, it cannot be altered once received.
 */
data class SettlementResponse(
    val id: UUID = UUID.randomUUID(),
    val batchId: UUID,
    val fileId: UUID,
    val result: ResponseResult,
    val messageCode: String,
    val description: String,
    val rejectionReason: RejectionReason? = null,
    val receivedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    init {
        require(messageCode.isNotBlank()) { "Message code cannot be empty" }
        if (result == ResponseResult.REJECTED) {
            requireNotNull(rejectionReason) {
                "Rejection reason is mandatory when the result is REJECTED"
            }
        }
    }

    val isAccepted: Boolean get() = result == ResponseResult.ACCEPTED
    val isRejected: Boolean get() = result == ResponseResult.REJECTED
}
