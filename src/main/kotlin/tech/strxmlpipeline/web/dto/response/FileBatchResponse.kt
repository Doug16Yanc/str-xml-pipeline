package tech.strxmlpipeline.web.dto.response

import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.model.FileBatch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class FileBatchResponse(
    val id: UUID,
    val windowKey: String,
    val referenceDate: LocalDate,
    val status: BatchStatus,
    val totalOrders: Int,
    val totalAmount: BigDecimal,
    val generatedAt: OffsetDateTime,
    val sentAt: OffsetDateTime?,
) {
    companion object {
        fun from(b: FileBatch) = FileBatchResponse(
            id            = b.id,
            windowKey     = b.window.partitioningKey,
            referenceDate = b.referenceDate,
            status        = b.status,
            totalOrders   = b.totalOrders,
            totalAmount   = b.totalAmount,
            generatedAt   = b.createdAt,
            sentAt        = null,
        )
    }
}