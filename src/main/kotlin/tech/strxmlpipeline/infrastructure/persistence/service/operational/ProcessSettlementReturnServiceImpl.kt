package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.domain.port.`in`.ProcessSettlementReturnUseCase
import tech.strxmlpipeline.domain.port.out.BatchCompensationPublisherPort
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.domain.port.out.SettlementReturnPort
import tech.strxmlpipeline.infrastructure.exception.local.DuplicateSettlementReturnException

@Service
@Transactional
class ProcessSettlementReturnServiceImpl(
    private val batchPort: FileBatchPort,
    private val orderPort: SettlementOrderPort,
    private val returnPort: SettlementReturnPort,
    private val compensationPublisher: BatchCompensationPublisherPort,
) : ProcessSettlementReturnUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(response: SettlementReturn) {
        val batch = requireNotNull(batchPort.findByIdWithOrders(response.batchId)) {
            "FileBatch not found for return processing: ${response.batchId}"
        }

        val existing = returnPort.findByBatchId(response.batchId)
        if (existing != null) {
            log.warn(
                "Return for batch [{}] already processed [result={}] — skipping",
                response.batchId, existing.result,
            )
            throw DuplicateSettlementReturnException(response.batchId)
        }

        returnPort.save(response)

        // EMITTED -> TRANSMITTED is implicit here until a dedicated transmission
        // consumer exists (Transmission Layer) — receiving an async return proves
        // the XML did reach BACEN/STR, so we fold the two transitions into one call.
        val transmitted = batch.transmit()
        val updatedBatch = if (response.isAccepted) transmitted.accept() else transmitted.rejectTransmission()
        batchPort.updateStatus(updatedBatch)

        val orders = orderPort.findByBatchId(response.batchId)
        val updatedOrders = if (response.isAccepted) {
            orders.map { it.accept() }
        } else {
            orders.map { it.reject() }
        }
        orderPort.updateStatusBatch(updatedOrders)

        // TRANSMISSION_REJECTED is where the choreographed compensation saga begins.
        // No orchestrator call — we just publish the trigger after commit and the
        // CompensatingConsumer picks it up independently.
        if (!response.isAccepted) {
            publishCompensationAfterCommit(updatedBatch, response)
        }

        log.info(
            "Settlement return processed — batch [{}] result [{}] reason [{}]",
            response.batchId, response.result, response.rejectionReason?.code ?: "n/a",
        )
    }

    /**
     * Publishes only after the enclosing transaction commits — publishing
     * before commit risks announcing a state that a rollback later undoes
     * (dual-write problem). Same pattern as ParticipantBatchProcessor.
     */
    private fun publishCompensationAfterCommit(batch: FileBatch, response: SettlementReturn) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    compensationPublisher.publishCompensation(batch, response.rejectionReason)
                }
            })
        } else {
            compensationPublisher.publishCompensation(batch, response.rejectionReason)
        }
    }
}