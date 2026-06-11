package tech.strxmlpipeline.infrastructure.messaging.xml

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.port.out.XmlGeneratorPort
import tech.strxmlpipeline.infrastructure.exception.local.XmlGenerationException
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory

@Service
class JaxbXmlGeneratorService(
    private val resourceLoader: ResourceLoader,
    @Value("\${str.xsd.path:classpath:xsd/str-settlement-2.0.xsd}") private val xsdPath: String,
    @Value("\${str.xsd.version:2.0}") val xsdVersion: String,
) : XmlGeneratorPort {

    private val log = LoggerFactory.getLogger(javaClass)

    private val jaxbContext: JAXBContext by lazy {
        JAXBContext.newInstance(StrSettlementDocument::class.java)
    }

    private val schema by lazy {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        schemaFactory.newSchema(resourceLoader.getResource(xsdPath).url)
    }

    override fun generate(batch: FileBatch): ByteArray {
        val document = batch.toXmlDocument()
        val output = ByteArrayOutputStream()

        try {
            val marshaller = jaxbContext.createMarshaller().apply {
                setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
                this.schema = this@JaxbXmlGeneratorService.schema
            }
            marshaller.marshal(document, output)
        } catch (e: JAXBException) {
            logJaxbDiagnostics(e)
            throw XmlGenerationException(
                "Failed to generate XML for batch [${batch.id}]: ${e.message ?: "Check diagnostics in logs"}"
            )
        }

        val bytes = output.toByteArray()
        log.info(
            "Generated XML for batch [{}] — {} orders — {} bytes",
            batch.id, batch.totalOrders, bytes.size,
        )
        return bytes
    }

    /**
     * Extracts deep diagnostic details from JAXBExceptions, parsing linked exceptions
     * and metadata specific to the JAXB Reference Implementation via reflection.
     */
    private fun logJaxbDiagnostics(exception: JAXBException) {
        log.error("--- [JAXB ERROR DIAGNOSTIC START] ---")
        log.error("Main JAXB Exception: ${exception.message}", exception)

        var linkedEx = exception.linkedException
        while (linkedEx != null) {
            log.error("Linked Cause: ${linkedEx.message}", linkedEx)

            if (linkedEx.javaClass.name == "com.sun.xml.bind.v2.runtime.IllegalAnnotationExceptions") {
                try {
                    val getErrorsMethod = linkedEx.javaClass.getMethod("getErrors")
                    val errorsList = getErrorsMethod.invoke(linkedEx) as? List<*>
                    errorsList?.forEach { annotationError ->
                        val getMessageMethod = annotationError?.javaClass?.getMethod("toString")
                        val errorMessage = getMessageMethod?.invoke(annotationError)
                        log.error("[JAXB ANNOTATION ERROR DETAIL] -> $errorMessage")
                    }
                } catch (reflectionEx: Exception) {
                    log.error("Failed to extract JAXB annotation errors via reflection: ${reflectionEx.message}")
                }
            }
            linkedEx = try { linkedEx.cause } catch (_: Exception) { null }
        }
        log.error("--- [JAXB ERROR DIAGNOSTIC END] ---")
    }

    /**
     * Maps [FileBatch] to the JAXB document.
     * Orders are sorted by [endToEndId] to guarantee a deterministic byte sequence —
     * same logical content always produces the same SHA-256 regardless of insertion order.
     * This is the foundation of domain-level idempotency.
     */
    private fun FileBatch.toXmlDocument(): StrSettlementDocument {
        val sortedOrders = orders.sortedBy { it.endToEndId }

        return StrSettlementDocument(
            header = StrSettlementHeader(
                batchId        = id.toString(),
                window         = window.partitioningKey,
                referenceDate  = referenceDate,
                totalOrders    = totalOrders,
                totalAmount    = totalAmount,
                generatedAt    = OffsetDateTime.now(),
            ),
            orders = sortedOrders.map { order ->
                StrSettlementOrderEntry(
                    id              = order.id.toString(),
                    endToEndId      = order.endToEndId,
                    orderType       = order.type.code,
                    originatorIspb  = order.originator.ispb.value,
                    destinationIspb = order.destination.ispb.value,
                    amount          = order.amount,
                    currency        = "BRL",
                    settlementDate  = order.settlementDate,
                )
            }
        )
    }
}