package tech.strxmlpipeline.infrastructure.persistence.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.FileBatchPublisherPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import java.time.Clock
import java.time.LocalDate

@Component
class ParticipantBatchProcessor(
    private val orderPort: SettlementOrderPort,
    private val batchPort: FileBatchPort,
    private val publisherPort: FileBatchPublisherPort,
    private val clock: Clock
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // REQUIRES_NEW garante que cada participante ganha uma transação isolada do banco
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun process(window: SettlementWindow, today: LocalDate, participant: Participant) {

        if (!window.isOpen(clock)) {
            rejectPendingOrders(window, today, participant)
            return
        }

        if (batchPort.existsActiveBatch(window, today, participant.id)) {
            log.warn("FileBatch already exists for window [{}] date [{}] participant [{}]. Skipping.",
                window.partitioningKey, today, participant.ispb)
            return
        }

        val pendingOrders = orderPort.findPendingForWindow(window, today, participant.ispb)

        if (pendingOrders.isEmpty()) {
            log.trace("No pending orders for participant [{}] window [{}]", participant.ispb, window.partitioningKey)
            return
        }

        val batchedOrders = pendingOrders.map { it.batch() }

        val batch = FileBatch(
            window        = window,
            referenceDate = today,
            orders        = batchedOrders,
            status        = BatchStatus.PENDING,
            participant   = participant,
        )

        val savedBatch = batchPort.save(batch)

        val ordersWithBatch = batchedOrders.map { it.associateWithBatch(savedBatch.id) }
        orderPort.updateStatusBatch(ordersWithBatch)

        // Como o método termina aqui, o commit acontece IMEDIATAMENTE e o Kafka é disparado
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    publisherPort.publish(savedBatch)
                    log.info("FileBatch [{}] published to Kafka for participant [{}].", savedBatch.id, participant.ispb)
                }
            })
        } else {
            publisherPort.publish(savedBatch)
        }

        log.info(
            "FileBatch [{}] assembled — participant [{}] window [{}] — {} orders — total: {}",
            savedBatch.id, participant.ispb, window.partitioningKey,
            savedBatch.totalOrders, savedBatch.totalAmount,
        )
    }

    private fun rejectPendingOrders(window: SettlementWindow, date: LocalDate, participant: Participant) {
        val pending = orderPort.findPendingForWindow(window, date, participant.ispb)
        if (pending.isEmpty()) return

        val rejected = pending.map { it.rejectCutoff() }
        orderPort.updateStatusOnly(rejected)

        log.warn("Marked {} orders as REJECTED_CUTOFF — participant [{}] window [{}]", rejected.size, participant.ispb, window.partitioningKey)
    }
}