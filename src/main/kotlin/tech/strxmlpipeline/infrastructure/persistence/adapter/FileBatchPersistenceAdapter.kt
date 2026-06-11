package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toFileBatchDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toFileBatchEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.FileBatchJpaRepository
import java.time.LocalDate
import java.util.UUID

@Component
@Transactional
class FileBatchPersistenceAdapter(
    private val batchJpaRepository: FileBatchJpaRepository
) : FileBatchPort {

    override fun save(batch: FileBatch): FileBatch {
        val entity = batch.toFileBatchEntity()
        batchJpaRepository.save(entity)
        return batch
    }

    override fun findByIdWithOrders(id: UUID): FileBatch? {
        return batchJpaRepository.findByIdWithOrders(id)?.toFileBatchDomain()
    }

    override fun findByWindowAndDate(window: SettlementWindow, date: LocalDate): List<FileBatch> {
        return batchJpaRepository.findByWindowAndReferenceDate(
            window = window.partitioningKey,
            date = date)
            .map { it.toFileBatchDomain() }
    }

    override fun updateStatus(batch: FileBatch): FileBatch {
        val existing = batchJpaRepository.findById(batch.id)
            .orElseThrow { NoSuchElementException("Batch not found: ${batch.id}") }

        existing.status = BatchStatus.valueOf(batch.status.name)

        return batchJpaRepository.save(existing).toFileBatchDomain()
    }

    override fun existsActiveBatch(window: SettlementWindow, date: LocalDate, participantId: UUID): Boolean {
        return batchJpaRepository.existsActiveBatchForWindowDateAndParticipant(
            window = window.partitioningKey,
            date = date,
            participantId = participantId
        )
    }
}