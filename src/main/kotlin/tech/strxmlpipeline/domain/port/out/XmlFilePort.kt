package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.S3Key
import tech.strxmlpipeline.domain.model.XmlFile
import java.util.UUID

interface XmlFilePort {
    fun save(file: XmlFile): XmlFile
    fun findByChecksum(checksumSha256: String): XmlFile?
    fun findByBatch(batchId: UUID): XmlFile?
    fun upload(fileContent: ByteArray, s3Key: S3Key): S3Key
    fun download(s3Key: S3Key): ByteArray
}