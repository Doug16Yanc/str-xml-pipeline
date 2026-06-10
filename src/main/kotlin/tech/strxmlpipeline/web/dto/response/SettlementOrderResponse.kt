package tech.strxmlpipeline.web.dto.response

import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.SettlementOrder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class SettlementOrderResponse(
    val id: UUID,
    val orderType: String,
    val amount: BigDecimal,
    val settlementDate: LocalDate,
    val originatorIspb: String,
    val destinationIspb: String,
    val endToEndId: String,
    val status: OrderStatus,
    val batchId: UUID?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(o: SettlementOrder, batchId: UUID? = null) = SettlementOrderResponse(
            id = o.id,
            orderType = o.type.code,
            amount = o.amount,
            settlementDate = o.settlementDate,
            originatorIspb = o.originator.ispb.value,
            destinationIspb = o.destination.ispb.value,
            endToEndId = o.endToEndId,
            status = o.status,
            batchId = batchId,
            createdAt = o.createdAt,
            updatedAt = o.updatedAt,
        )
    }
}
