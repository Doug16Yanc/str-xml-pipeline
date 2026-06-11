package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter : XmlAdapter<String, OffsetDateTime>() {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun marshal(v: OffsetDateTime?): String? {
        return v?.format(formatter)
    }

    override fun unmarshal(v: String?): OffsetDateTime? {
        return v?.let { OffsetDateTime.parse(it, formatter) }
    }
}