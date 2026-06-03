package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.S3Key
import tech.strxmlpipeline.domain.model.XmlFile
import tech.strxmlpipeline.domain.port.out.XmlFilePort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.XmlFileJpaRepository
import java.util.UUID

@Component
@Transactional
class XmlFilePersistenceAdapter(
    private val fileJpaRepository: XmlFileJpaRepository
) : XmlFilePort {

    override fun save(file: XmlFile): XmlFile {
        return fileJpaRepository.save(file.toEntity()).toDomain()
    }

    override fun findByChecksum(checksumSha256: String): XmlFile? {
        return fileJpaRepository.findByChecksumSha256(checksumSha256)?.toDomain()
    }

    override fun findByBatch(batchId: UUID): XmlFile? {
        return fileJpaRepository.findByBatchId(batchId)?.toDomain()
    }

    override fun upload(fileContent: ByteArray, s3Key: S3Key): S3Key {
        println("Simulating physical upload to AWS S3: ${s3Key.value}")
        return s3Key
    }

    override fun download(s3Key: S3Key): ByteArray {
        return ByteArray(0)
    }
}