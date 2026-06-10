package tech.strxmlpipeline.web.dto.request

import tech.strxmlpipeline.domain.model.OrderType
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.model.SettlementOrder
import java.math.BigDecimal
import java.time.LocalDate

data class CreateSettlementOrderRequest(
    val orderType: String,
    val amount: BigDecimal,
    val settlementDate: LocalDate,
    val originatorIspb: String,
    val destinationIspb: String,
    val endToEndId: String,
) {
    fun toDomain(
        originator: Participant,
        destination: Participant,
    ) = SettlementOrder(
        type = OrderType.fromCode(orderType),
        amount = amount,
        settlementDate = settlementDate,
        originator = originator,
        destination = destination,
        endToEndId = endToEndId,
    )
}
