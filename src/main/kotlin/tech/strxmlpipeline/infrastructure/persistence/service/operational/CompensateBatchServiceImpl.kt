package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.port.`in`.CompensateBatchUseCase
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import java.util.UUID

/**
 * Choreographed compensation step of the Terceira Milha saga. No orchestrator —
 * this service only reacts to a single batchId handed to it by the
 * CompensatingConsumer and moves that one batch through
 * TRANSMISSION_REJECTED -> COMPENSATING -> COMPENSATED.
 *
 * Idempotency is guarded two ways, both reusing FileBatch's own persisted
 * state (see BatchStatus / FileBatchEntity.version) rather than a separate
 * dedup key:
 *  1. Cheap short-circuit: skip outright if status isn't TRANSMISSION_REJECTED.
 *  2. Safety net: optimistic-lock conflict on save (concurrent redelivery)
 *     is caught and treated as "someone else is already compensating this".
 */
@Service
@Transactional
class CompensateBatchServiceImpl(
    private val batchPort: FileBatchPort,
    private val orderPort: SettlementOrderPort,
) : CompensateBatchUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun compensate(batchId: UUID) {
        val batch = batchPort.findByIdWithOrders(batchId)
        if (batch == null) {
            log.warn("Compensation skipped — batch [{}] not found", batchId)
            return
        }

        if (batch.status != BatchStatus.TRANSMISSION_REJECTED) {
            log.info(
                "Compensation skipped — batch [{}] already in status [{}] (redelivery or race — safe no-op)",
                batchId, batch.status,
            )
            return
        }

        val compensating = try {
            batch.startCompensation()
        } catch (e: IllegalStateException) {
            log.info("Compensation skipped — batch [{}] failed state transition (race): {}", batchId, e.message)
            return
        }

        try {
            batchPort.updateStatus(compensating)
        } catch (e: OptimisticLockingFailureException) {
            log.info(
                "Compensation skipped — batch [{}] lost the optimistic-lock race, another delivery is handling it",
                batchId,
            )
            return
        }

        val rejectedOrders = orderPort.findByBatchId(batchId).filter { it.status == OrderStatus.REJECTED }

        if (rejectedOrders.isEmpty()) {
            log.warn(
                "Batch [{}] entered compensation with no REJECTED orders left to release — " +
                        "likely a partial retry of a previous compensation. Closing it out anyway.",
                batchId,
            )
        } else {
            val released = rejectedOrders.map { it.releaseForCompensation() }
            orderPort.releaseForCompensation(released)
            log.info(
                "Released {} order(s) back to PENDING for batch [{}] — next SettlementWindow cycle picks them up",
                released.size, batchId,
            )
        }

        batchPort.updateStatus(compensating.completeCompensation())

        log.info("Batch [{}] compensation complete", batchId)
    }
}