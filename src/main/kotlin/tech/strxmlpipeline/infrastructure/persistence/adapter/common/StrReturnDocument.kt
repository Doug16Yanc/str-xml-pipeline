package tech.strxmlpipeline.infrastructure.persistence.adapter.common

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement


@XmlRootElement(name = "StrReturnMessage")
@XmlAccessorType(XmlAccessType.FIELD)
data class StrReturnDocument(
    @XmlElement(name = "BatchId")        val batchId: String?      = null,
    @XmlElement(name = "Result")         val result: String?       = null,
    @XmlElement(name = "MessageCode")    val messageCode: String?  = null,
    @XmlElement(name = "RejectionCode")  val rejectionCode: String? = null,
    @XmlElement(name = "Description")    val description: String?  = null
)