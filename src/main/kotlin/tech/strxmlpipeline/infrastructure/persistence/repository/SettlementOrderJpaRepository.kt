package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.infrastructure.persistence.entity.SettlementOrderEntity
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface SettlementOrderJpaRepository : JpaRepository<SettlementOrderEntity, UUID> {

    fun findByStatus(status: OrderStatus): List<SettlementOrderEntity>

    @Query("""
        SELECT o FROM SettlementOrderEntity o 
        WHERE o.status = tech.strxmlpipeline.domain.enum.OrderStatus.PENDING 
        AND o.settlementDate = :date 
        AND o.window = :window 
        AND o.batch IS NULL
    """)
    fun findPendingOrdersForWindow(
        @Param("window") window: String,
        @Param("date") date: LocalDate
    ): List<SettlementOrderEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SettlementOrderEntity o 
        SET o.status = :status,
            o.batch.id = :batchId,
            o.version = o.version + 1,
            o.updatedAt = :now
        WHERE o.id IN :ids
    """)
    fun updateStatusAndBatchIdForIds(
        @Param("ids") ids: List<UUID>,
        @Param("status") status: OrderStatus,
        @Param("batchId") batchId: UUID,
        @Param("now") now: OffsetDateTime
    ): Int

    fun findByBatchId(batchId: UUID): List<SettlementOrderEntity>
}