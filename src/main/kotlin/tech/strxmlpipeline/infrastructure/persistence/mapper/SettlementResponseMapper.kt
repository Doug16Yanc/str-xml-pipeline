package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.enum.ResponseResult
import tech.strxmlpipeline.domain.model.SettlementResponse
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementResponseEntity
import java.time.ZoneOffset

fun SettlementResponseEntity.toDomain(): SettlementResponse = SettlementResponse(
    id = this.id,
    batchId = this.batchId,
    fileId = this.id,
    result = ResponseResult.valueOf(this.status.name),
    messageCode = this.responseCode,
    description = this.description,
    rejectionReason = if (this.status == ResponseResult.REJECTED) RejectionReason.fromCode(this.responseCode) else null,
    receivedAt = this.receivedAt
)

fun SettlementResponse.toEntity(): SettlementResponseEntity = SettlementResponseEntity(
    id = this.id,
    batchId = this.batchId,
    responseCode = this.rejectionReason?.code ?: this.messageCode,
    description = this.description,
    status = ResponseResult.valueOf(result.name),
    receivedAt = this.receivedAt
)