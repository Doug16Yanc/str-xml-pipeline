package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.math.BigDecimal
import java.time.LocalDate

@XmlAccessorType(XmlAccessType.FIELD)
data class StrSettlementOrderEntry(
    @field:XmlElement(name = "Id", required = true)
    val id: String = "",

    @field:XmlElement(name = "EndToEndId", required = true)
    val endToEndId: String = "",

    @field:XmlElement(name = "OrderType", required = true)
    val orderType: String = "",

    @field:XmlElement(name = "OriginatorIspb", required = true)
    val originatorIspb: String = "",

    @field:XmlElement(name = "DestinationIspb", required = true)
    val destinationIspb: String = "",

    @field:XmlElement(name = "Amount", required = true)
    val amount: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(name = "Currency", required = true)
    val currency: String = "BRL",

    @field:XmlElement(name = "SettlementDate", required = true)
    @field:XmlJavaTypeAdapter(LocalDateAdapter::class)
    val settlementDate: LocalDate = LocalDate.now(),
)