package tech.strxmlpipeline.web.dto.response

import tech.strxmlpipeline.domain.enum.ReturnResult
import tech.strxmlpipeline.domain.model.SettlementReturn
import java.time.OffsetDateTime
import java.util.UUID

data class SettlementReturnResponse(
    val id: UUID,
    val batchId: UUID,
    val fileId: UUID,
    val result: ReturnResult,
    val messageCode: String,
    val description: String,
    val rejectionReason: RejectionReasonResponse?,
    val receivedAt: OffsetDateTime,
) {
    companion object {
        fun from(r: SettlementReturn) = SettlementReturnResponse(
            id              = r.id,
            batchId         = r.batchId,
            fileId          = r.fileId,
            result          = r.result,
            messageCode     = r.messageCode,
            description     = r.description,
            rejectionReason = r.rejectionReason?.let {
                RejectionReasonResponse(code = it.code, description = it.description)
            },
            receivedAt      = r.receivedAt,
        )
    }
}
