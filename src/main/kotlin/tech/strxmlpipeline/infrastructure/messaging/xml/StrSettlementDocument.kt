package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "StrSettlement")
@XmlAccessorType(XmlAccessType.FIELD)
data class StrSettlementDocument(
    @XmlElement(name = "Header", required = true)
    val header: StrSettlementHeader = StrSettlementHeader(),

    @XmlElementWrapper(name = "Orders")
    @XmlElement(name = "Order")
    val orders: List<StrSettlementOrderEntry> = emptyList()
)