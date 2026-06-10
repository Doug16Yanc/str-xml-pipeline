package tech.strxmlpipeline.infrastructure.persistence.service.operational

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.port.`in`.AssembleFileBatchUseCase
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.FileBatchPublisherPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import java.time.Clock
import java.time.LocalDate

@Service
class AssembleFileBatchServiceImpl(
    private val orderPort: SettlementOrderPort,
    private val batchPort: FileBatchPort,
    private val publisherPort: FileBatchPublisherPort,
    private val clock: Clock,
    @PersistenceContext private val entityManager: EntityManager
) : AssembleFileBatchUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun assemble(windowKey: String) {
        val window = SettlementWindow.parse(windowKey)
        val today  = LocalDate.now(clock)

        entityManager.clear()

        val pendingOrders = orderPort.findPendingForWindow(window, today)

        if (pendingOrders.isEmpty()) {
            log.info("No pending orders found for window [{}] on [{}]", windowKey, today)
            return
        }
        val pendingBatchedOrders = pendingOrders.map { it.batch() }

        val batch = FileBatch(
            window        = window,
            referenceDate = today,
            orders        = pendingBatchedOrders,
            status        = BatchStatus.PENDING,
        )
        val savedBatch = batchPort.save(batch)

        val finalOrdersWithBatch = pendingBatchedOrders.map { order ->
            order.associateWithBatch(savedBatch.id)
        }
        orderPort.updateStatusBatch(finalOrdersWithBatch)

        entityManager.flush()

        publisherPort.publish(savedBatch)

        log.info(
            "FileBatch [{}] assembled for window [{}] — {} orders, total amount: {}",
            savedBatch.id, windowKey, savedBatch.totalOrders, savedBatch.totalAmount,
        )
    }

    /**
     * When cutoff is exceeded, mark all still-pending orders as REJECTED_CUTOFF
     * so the audit trail distinguishes internal rejection from BACEN rejection.
     */
    private fun rejectPendingOrders(window: SettlementWindow, date: LocalDate) {
        val pending = orderPort.findPendingForWindow(window, date)
        if (pending.isEmpty()) return

        val rejected = pending.map { it.rejectCutoff() }
        orderPort.updateStatusBatch(rejected)

        log.warn(
            "Marked {} orders as REJECTED_CUTOFF for window [{}]",
            rejected.size, window.partitioningKey,
        )
    }
}