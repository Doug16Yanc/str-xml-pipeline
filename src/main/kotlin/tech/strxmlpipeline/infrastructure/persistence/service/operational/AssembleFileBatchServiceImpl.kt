package tech.strxmlpipeline.infrastructure.persistence.service.operational

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.`in`.AssembleFileBatchUseCase
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.FileBatchPublisherPort
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import java.time.Clock
import java.time.LocalDate

@Service
class AssembleFileBatchServiceImpl(
    private val orderPort: SettlementOrderPort,
    private val batchPort: FileBatchPort,
    private val publisherPort: FileBatchPublisherPort,
    private val participantPort: ParticipantPort,
    private val clock: Clock,
    @PersistenceContext private val entityManager: EntityManager,
) : AssembleFileBatchUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun assemble(windowKey: String) {
        val window = SettlementWindow.parse(windowKey)
        val today  = LocalDate.now(clock)

        entityManager.clear()

        val participants = participantPort.findAll()
            .filter { it.type != ParticipantType.BACEN }

        if (participants.isEmpty()) {
            log.info("No participants found for window [{}]", windowKey)
            return
        }

        participants.forEach { participant ->
            assembleForParticipant(window, today, participant)
        }
    }

    private fun assembleForParticipant(
        window: SettlementWindow,
        today: LocalDate,
        participant: Participant,
    ) {
        if (!window.isOpen(clock)) {
            rejectPendingOrders(window, today, participant)
            return
        }

        val pendingOrders = orderPort.findPendingForWindow(window, today, participant.ispb)

        if (pendingOrders.isEmpty()) {
            log.debug(
                "No pending orders for participant [{}] window [{}]",
                participant.ispb, window.partitioningKey,
            )
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

        entityManager.flush()

        publisherPort.publish(savedBatch)

        log.info(
            "FileBatch [{}] assembled — participant [{}] window [{}] — {} orders — total: {}",
            savedBatch.id, participant.ispb, window.partitioningKey,
            savedBatch.totalOrders, savedBatch.totalAmount,
        )
    }

    private fun rejectPendingOrders(
        window: SettlementWindow,
        date: LocalDate,
        participant: Participant,
    ) {
        val pending = orderPort.findPendingForWindow(window, date, participant.ispb)
        if (pending.isEmpty()) return
        val rejected = pending.map { it.rejectCutoff() }
        orderPort.updateStatusBatch(rejected)
        log.warn(
            "Marked {} orders as REJECTED_CUTOFF — participant [{}] window [{}]",
            rejected.size, participant.ispb, window.partitioningKey,
        )
    }
}