package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : XmlAdapter<String, LocalDate>() {
    override fun marshal(v: LocalDate?): String? =
        v?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    override fun unmarshal(v: String?): LocalDate? =
        v?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
}