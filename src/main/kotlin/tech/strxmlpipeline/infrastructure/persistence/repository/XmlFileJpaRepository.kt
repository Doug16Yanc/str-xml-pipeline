package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.XmlFileEntity
import java.util.UUID

@Repository
interface XmlFileJpaRepository : JpaRepository<XmlFileEntity, UUID> {
    fun findByChecksumSha256(checksumSha256: String): XmlFileEntity?
    fun findByBatchId(batchId: UUID): XmlFileEntity?
}