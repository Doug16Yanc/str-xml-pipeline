package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementReturnEntity

fun SettlementReturnEntity.toSettlementReturnDomain(): SettlementReturn = SettlementReturn(
    id = this.id,
    batchId = this.batchId,
    fileId = this.fileId,
    result = this.result,
    messageCode = this.messageCode,
    description = this.description,
    rejectionReason = this.rejectionReason,
    receivedAt = this.receivedAt
)

fun SettlementReturn.toSettlementReturnEntity(): SettlementReturnEntity = SettlementReturnEntity(
    id = this.id,
    batchId = this.batchId,
    fileId = this.fileId,
    result = this.result,
    messageCode = this.messageCode,
    description = this.description,
    rejectionReason = this.rejectionReason,
    receivedAt = this.receivedAt
)