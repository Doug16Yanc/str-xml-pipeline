package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.CharJdbcType
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "xml_file")
class XmlFileEntity(
    @Id
    @Column(name = "id", updatable = false)
    val id: UUID,

    @Column(name = "batch_id", nullable = false, updatable = false, unique = true)
    val batchId: UUID,

    @Column(name = "s3_bucket", length = 63, nullable = false, updatable = false)
    val s3Bucket: String,

    @Column(name = "s3_key", length = 512, nullable = false, updatable = false)
    val s3Key: String,

    @Column(name = "checksum_sha256", columnDefinition = "char(64)", length = 64, nullable = false)
    @JdbcType(CharJdbcType::class)
    val checksumSha256: String,

    @Column(name = "size_bytes", nullable = false, updatable = false)
    val sizeBytes: Long,

    @Column(name = "xsd_version", length = 20, nullable = false, updatable = false)
    val xsdVersion: String,

    @Column(name = "emitted_at", nullable = false, updatable = false)
    val emittedAt: OffsetDateTime = OffsetDateTime.now()
)