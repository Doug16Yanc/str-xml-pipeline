package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.SettlementReturn
import tech.strxmlpipeline.domain.port.out.SettlementReturnPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toSettlementReturnDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toSettlementReturnEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.SettlementReturnJpaRepository
import java.util.UUID

@Component
@Transactional
class SettlementReturnPersistenceAdapter(
    private val responseJpaRepository: SettlementReturnJpaRepository
) : SettlementReturnPort {

    override fun save(returnModel: SettlementReturn): SettlementReturn {
        val entity = returnModel.toSettlementReturnEntity()

        val savedEntity = responseJpaRepository.save(entity)

        return returnModel.copy(id = savedEntity.id)
    }

    override fun findById(id: UUID): SettlementReturn? {
        return responseJpaRepository.findById(id)
            .map { it.toSettlementReturnDomain() }
            .orElse(null)
    }

    override fun findByBatchId(batchId: UUID): SettlementReturn? {
        return responseJpaRepository.findByBatchId(batchId)?.toSettlementReturnDomain()
    }
}