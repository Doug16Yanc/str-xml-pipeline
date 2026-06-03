package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity
import java.time.LocalDate
import java.util.UUID

@Repository
interface FileBatchJpaRepository : JpaRepository<FileBatchEntity, UUID> {
    fun findByWindowAndReferenceDate(window: SettlementWindow, referenceDate: LocalDate): List<FileBatchEntity>
}