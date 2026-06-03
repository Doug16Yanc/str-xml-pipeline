package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.FileBatchJpaRepository
import java.time.LocalDate
import java.util.UUID

@Component
@Transactional
class FileBatchPersistenceAdapter(
    private val batchJpaRepository: FileBatchJpaRepository
) : FileBatchPort {

    override fun save(batch: FileBatch): FileBatch {
        val entity = batch.toEntity()
        return batchJpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): FileBatch? {
        return batchJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByWindowAndDate(window: SettlementWindow, date: LocalDate): List<FileBatch> {
        return batchJpaRepository.findByWindowAndReferenceDate(window, date)
            .map { it.toDomain() }
    }

    override fun updateStatus(batch: FileBatch): FileBatch {
        val existing = batchJpaRepository.findById(batch.id)
            .orElseThrow { NoSuchElementException("Batch not found: ${batch.id}") }

        existing.status = BatchStatus.valueOf(batch.status.name)

        return batchJpaRepository.save(existing).toDomain()
    }
}