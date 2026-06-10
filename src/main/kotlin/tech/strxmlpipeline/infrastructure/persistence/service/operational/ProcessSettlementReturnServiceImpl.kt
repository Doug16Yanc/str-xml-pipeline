package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.domain.port.`in`.ProcessSettlementReturnUseCase
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
) : ProcessSettlementReturnUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(response: SettlementReturn) {
        val batch = requireNotNull(batchPort.findById(response.batchId)) {
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

        val updatedBatch = if (response.isAccepted) batch.confirm() else batch.reject()
        batchPort.updateStatus(updatedBatch)

        val orders = orderPort.findByBatchId(response.batchId)
        val updatedOrders = if (response.isAccepted) {
            orders.map { it.confirm() }
        } else {
            orders.map { it.reject() }
        }
        orderPort.updateStatusBatch(updatedOrders)

        log.info(
            "Settlement return processed — batch [{}] result [{}] reason [{}]",
            response.batchId, response.result, response.rejectionReason?.code ?: "n/a",
        )
    }
}