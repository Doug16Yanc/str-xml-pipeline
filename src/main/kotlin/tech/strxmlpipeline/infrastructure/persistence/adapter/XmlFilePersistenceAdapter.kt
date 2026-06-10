package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.XmlFile
import tech.strxmlpipeline.domain.port.out.XmlFilePort
import tech.strxmlpipeline.domain.valueobject.S3Key
import tech.strxmlpipeline.infrastructure.persistence.mapper.toXmlFileDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toXmlFileEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.XmlFileJpaRepository
import java.util.UUID

@Component
@Transactional
class XmlFilePersistenceAdapter(
    private val fileJpaRepository: XmlFileJpaRepository
) : XmlFilePort {

    override fun save(file: XmlFile): XmlFile {
        return fileJpaRepository.save(file.toXmlFileEntity()).toXmlFileDomain()
    }

    override fun findById(id: UUID): XmlFile? {
        return fileJpaRepository.findById(id)
            .map { it.toXmlFileDomain() }
            .orElse(null)
    }

    override fun findByBatchId(batchId: UUID): XmlFile? {
        return fileJpaRepository.findByBatchId(batchId)?.toXmlFileDomain()
    }

    override fun findByChecksum(checksumSha256: String): XmlFile? {
        return fileJpaRepository.findByChecksumSha256(checksumSha256)?.toXmlFileDomain()
    }


}