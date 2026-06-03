package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementResponseEntity
import java.util.UUID

@Repository
interface SettlementResponseJpaRepository : JpaRepository<SettlementResponseEntity, UUID> {
    fun findByBatchId(batchId: UUID): SettlementResponseEntity?
}