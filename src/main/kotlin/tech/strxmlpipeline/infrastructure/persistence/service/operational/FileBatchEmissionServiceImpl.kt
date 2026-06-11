package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.XmlFile
import tech.strxmlpipeline.domain.port.`in`.FileBatchEmissionUseCase
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.domain.port.out.XmlFilePort
import tech.strxmlpipeline.domain.port.out.XmlFileStoragePort
import tech.strxmlpipeline.domain.port.out.XmlGeneratorPort
import tech.strxmlpipeline.domain.valueobject.S3Key
import java.security.MessageDigest
import java.util.UUID

@Service
class FileBatchEmissionServiceImpl(
    private val batchPort: FileBatchPort,
    private val orderPort: SettlementOrderPort,
    private val xmlFilePort: XmlFilePort,
    private val xmlGeneratorPort: XmlGeneratorPort,
    private val storagePort: XmlFileStoragePort,
    @Value("\${str.xsd.version:2.0}") private val xsdVersion: String,
    @Value("\${aws.s3.bucket}") private val s3Bucket: String,
) : FileBatchEmissionUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun emit(batchId: UUID) {
        val batch = requireNotNull(batchPort.findByIdWithOrders(batchId)) {
            "FileBatch not found: $batchId"
        }

        val existingFile = xmlFilePort.findByBatchId(batchId)
        if (existingFile != null) {
            log.warn(
                "FileBatch [{}] already has XmlFile [{}] — skipping (idempotent redelivery)",
                batchId, existingFile.id,
            )
            return
        }

        val xmlBytes = xmlGeneratorPort.generate(batch)
        val checksum = sha256Hex(xmlBytes)

        val checksumConflict = xmlFilePort.findByChecksum(checksum)
        if (checksumConflict != null) {
            log.warn(
                "Duplicate detected by checksum [{}] — existing file [{}] — short-circuiting",
                checksum, checksumConflict.id,
            )
            return
        }

        val s3Key = S3Key("$s3Bucket/str/${batch.window.partitioningKey}/${batch.referenceDate}/$batchId.xml")
        storagePort.upload(xmlBytes, s3Key)

        val xmlFile = XmlFile(
            batchId        = batchId,
            s3Key          = s3Key,
            checksumSha256 = checksum,
            xsdVersion     = xsdVersion,
            sizeBytes      = xmlBytes.size.toLong(),
        )
        xmlFilePort.save(xmlFile)

        batchPort.updateStatus(batch.emit())

        val orders = orderPort.findByBatchId(batchId).map { it.emit() }
        orderPort.updateStatusBatch(orders)

        log.info(
            "FileBatch [{}] emitted — s3Key [{}] — checksum [{}] — {} orders",
            batchId, s3Key, checksum, orders.size,
        )
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
}