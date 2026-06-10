package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.XmlFile
import tech.strxmlpipeline.domain.port.out.XmlFilePort
import tech.strxmlpipeline.domain.port.out.XmlFileStoragePort
import tech.strxmlpipeline.domain.valueobject.S3Key
import tech.strxmlpipeline.infrastructure.exception.local.XmlFileNotFoundException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class XmlFileServiceImpl(
    private val xmlFilePort: XmlFilePort,
) {

    fun findById(id: UUID): XmlFile =
        xmlFilePort.findById(id)
            ?: throw XmlFileNotFoundException("XmlFile not found: $id")

    fun findByBatchId(batchId: UUID): XmlFile =
        xmlFilePort.findByBatchId(batchId)
            ?: throw XmlFileNotFoundException("XmlFile not found for batch: $batchId")

    fun findByChecksum(checksum: String): XmlFile =
        xmlFilePort.findByChecksum(checksum)
            ?: throw XmlFileNotFoundException("XmlFile not found for checksum: $checksum")

}

