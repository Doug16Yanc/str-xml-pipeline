package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.SettlementResponse
import tech.strxmlpipeline.domain.port.out.SettlementResponsePort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.SettlementResponseJpaRepository
import java.util.UUID

@Component
@Transactional
class SettlementResponsePersistenceAdapter(
    private val responseJpaRepository: SettlementResponseJpaRepository
) : SettlementResponsePort {

    override fun save(response: SettlementResponse): SettlementResponse {
        return responseJpaRepository.save(response.toEntity()).toDomain()
    }

    override fun findByBatchId(batchId: UUID): SettlementResponse? {
        return responseJpaRepository.findByBatchId(batchId)?.toDomain()
    }
}