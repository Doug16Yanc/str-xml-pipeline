package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.infrastructure.persistence.entity.RawSettlementReturnEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.RawSettlementReturnJpaRepository
import java.time.OffsetDateTime
import java.util.UUID


@Service
class RawReturnPersistenceServiceImpl(
    private val repository: RawSettlementReturnJpaRepository,
) {

    /**
     * Persists the raw XML payload in a separate transaction (REQUIRES_NEW) so
     * the audit record is committed even if downstream processing fails and
     * the outer transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun persist(rawPayload: String, partition: Int, offset: Long): RawSettlementReturnEntity {
        val entity = RawSettlementReturnEntity(
            id = UUID.randomUUID(),
            rawPayload = rawPayload,
            partition = partition,
            offset = offset,
            receivedAt = OffsetDateTime.now(),
        )
        return repository.save(entity)
    }

    /**
     * Links the raw record to its parsed batchId once parsing succeeds.
     * Kept in a separate call so the raw record exists even if parsing fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun linkToBatch(rawId: UUID, batchId: UUID) {
        repository.linkToBatch(rawId, batchId)
    }
}
