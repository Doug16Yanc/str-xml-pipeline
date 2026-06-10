package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.domain.port.out.SettlementReturnPort
import tech.strxmlpipeline.infrastructure.exception.local.SettlementReturnNotFoundException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SettlementReturnServiceImpl(
    private val returnPort: SettlementReturnPort,
) {

    fun findById(id: UUID): SettlementReturn =
        returnPort.findById(id)
            ?: throw SettlementReturnNotFoundException(id)

    fun findByBatchId(batchId: UUID): SettlementReturn =
        returnPort.findByBatchId(batchId)
            ?: throw SettlementReturnNotFoundException(batchId)
}
