package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.XmlFile
import java.util.UUID

interface XmlFilePort {
    fun save(file: XmlFile): XmlFile
    fun findById(id: UUID): XmlFile?
    fun findByBatchId(batchId: UUID): XmlFile?
    fun findByChecksum(checksumSha256: String): XmlFile?
}