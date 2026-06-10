package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@XmlAccessorType(XmlAccessType.FIELD)
data class StrSettlementHeader(
    @XmlElement(name = "BatchId")
    val batchId: String = "",

    @XmlElement(name = "Window")
    val window: String = "",

    @XmlElement(name = "ReferenceDate")
    @XmlJavaTypeAdapter(LocalDateAdapter::class)
    val referenceDate: LocalDate = LocalDate.now(),

    @XmlElement(name = "TotalOrders")
    val totalOrders: Int = 0,

    @XmlElement(name = "TotalAmount")
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @XmlElement(name = "GeneratedAt")
    @XmlJavaTypeAdapter(LocalDateAdapter::class)
    val generatedAt: OffsetDateTime = OffsetDateTime.now(),
)
