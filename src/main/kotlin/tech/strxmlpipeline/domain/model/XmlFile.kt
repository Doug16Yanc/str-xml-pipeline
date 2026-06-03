package tech.strxmlpipeline.domain.model

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Physical artifact generated from a [FileBatch].
 * The [checksumSha256] acts as the idempotency anchor: if the consumer fails and
 * reprocesses, you can detect duplication before performing the upload to S3.
 */
data class XmlFile(
    val id: UUID = UUID.randomUUID(),
    val batchId: UUID,
    val s3Key: S3Key,
    val checksumSha256: String,
    val xsdVersion: String,
    val sizeBytes: Long,
    val generatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    init {
        require(checksumSha256.matches(Regex("[a-f0-9]{64}"))) {
            "Invalid SHA-256 checksum: must contain 64 lowercase hexadecimal characters"
        }
        require(xsdVersion.isNotBlank()) { "XSD version cannot be empty" }
        require(sizeBytes > 0) { "File size must be positive" }
    }

    val isDuplicate: Boolean get() = false
}

@JvmInline
value class S3Key(val value: String) {
    init {
        require(value.isNotBlank()) { "S3Key cannot be empty" }
    }

    override fun toString(): String = value
}
