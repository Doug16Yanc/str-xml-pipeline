package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementOrderEntity
import java.time.LocalDate
import java.util.UUID

@Repository
interface SettlementOrderJpaRepository : JpaRepository<SettlementOrderEntity, UUID> {

    @Query("""
        SELECT o FROM SettlementOrderEntity o 
        WHERE o.status = tech.strxmlpipeline.domain.enum.OrderStatus.PENDING 
        AND o.settlementDate = :date 
        AND o.batch IS NULL
    """)
    fun findPendingOrdersForWindow(@Param("date") date: LocalDate): List<SettlementOrderEntity>

    fun findByBatchId(batchId: UUID): List<SettlementOrderEntity>
}