package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.math.BigDecimal
import java.time.LocalDate

@XmlAccessorType(XmlAccessType.FIELD)
data class StrSettlementOrderEntry(
    @XmlElement(name = "Id")
    val id: String = "",

    @XmlElement(name = "EndToEndId")
    val endToEndId: String = "",

    @XmlElement(name = "OrderType")
    val orderType: String = "",

    @XmlElement(name = "OriginatorIspb")
    val originatorIspb: String = "",

    @XmlElement(name = "DestinationIspb")
    val destinationIspb: String = "",

    @XmlElement(name = "Amount")
    val amount: BigDecimal = BigDecimal.ZERO,

    @XmlElement(name = "Currency")
    val currency: String = "BRL",

    @XmlElement(name = "SettlementDate")
    @XmlJavaTypeAdapter(LocalDateAdapter::class)
    val settlementDate: LocalDate = LocalDate.now(),
)
