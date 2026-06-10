package tech.strxmlpipeline.infrastructure.persistence.service.operational

import jakarta.xml.bind.JAXBContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.valueobject.ParsedReturn
import tech.strxmlpipeline.infrastructure.exception.local.StrReturnParseException
import tech.strxmlpipeline.infrastructure.persistence.adapter.common.StrReturnDocument
import java.io.StringReader

@Service
class StrReturnXmlParserImpl {

    private val log = LoggerFactory.getLogger(javaClass)

    private val jaxbContext: JAXBContext by lazy {
        JAXBContext.newInstance(StrReturnDocument::class.java)
    }

    fun parse(rawXml: String): ParsedReturn {
        return try {
            val unmarshaller = jaxbContext.createUnmarshaller()
            val document = unmarshaller.unmarshal(StringReader(rawXml)) as StrReturnDocument

            ParsedReturn(
                batchId       = requireNotNull(document.batchId) { "Missing BatchId in STR return" },
                result        = requireNotNull(document.result)  { "Missing Result in STR return" },
                messageCode   = requireNotNull(document.messageCode) { "Missing MessageCode in STR return" },
                rejectionCode = document.rejectionCode?.takeIf { it.isNotBlank() },
                description = document.description
            )
        } catch (e: Exception) {
            log.error("Failed to parse STR return XML: {}", e.message)
            throw StrReturnParseException("Failed to parse STR return XML: ${e.message}", e)
        }
    }
}
