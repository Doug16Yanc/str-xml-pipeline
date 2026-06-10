package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter : XmlAdapter<String, OffsetDateTime>() {
    override fun marshal(v: OffsetDateTime?): String? =
        v?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    override fun unmarshal(v: String?): OffsetDateTime? =
        v?.let { OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
}