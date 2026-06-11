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
    @field:XmlElement(name = "BatchId", required = true)
    val batchId: String = "",

    @field:XmlElement(name = "Window", required = true)
    val window: String = "",

    @field:XmlElement(name = "ReferenceDate", required = true)
    @field:XmlJavaTypeAdapter(LocalDateAdapter::class)
    val referenceDate: LocalDate = LocalDate.now(),

    @field:XmlElement(name = "TotalOrders", required = true)
    val totalOrders: Int = 0,

    @field:XmlElement(name = "TotalAmount", required = true)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(name = "GeneratedAt", required = true)
    @field:XmlJavaTypeAdapter(OffsetDateTimeAdapter::class)
    val generatedAt: OffsetDateTime = OffsetDateTime.now(),
)