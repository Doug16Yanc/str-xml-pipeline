package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementReturnEntity
import java.util.UUID

@Repository
interface SettlementReturnJpaRepository : JpaRepository<SettlementReturnEntity, UUID> {
    fun findByBatchId(batchId: UUID): SettlementReturnEntity?
}