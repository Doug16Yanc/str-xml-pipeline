package tech.strxmlpipeline.web.dto.response

import tech.strxmlpipeline.domain.model.XmlFile
import java.time.OffsetDateTime
import java.util.UUID

data class XmlFileResponse(
    val id: UUID,
    val batchId: UUID,
    val s3Key: String,
    val checksumSha256: String,
    val xsdVersion: String,
    val sizeBytes: Long,
    val generatedAt: OffsetDateTime,
) {
    companion object {
        fun from(f: XmlFile) = XmlFileResponse(
            id             = f.id,
            batchId        = f.batchId,
            s3Key          = f.s3Key.value,
            checksumSha256 = f.checksumSha256,
            xsdVersion     = f.xsdVersion,
            sizeBytes      = f.sizeBytes,
            generatedAt    = f.generatedAt,
        )
    }
}
