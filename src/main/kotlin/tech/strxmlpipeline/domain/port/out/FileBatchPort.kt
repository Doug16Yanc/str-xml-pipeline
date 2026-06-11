package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import java.time.LocalDate
import java.util.UUID

interface FileBatchPort {
    fun save(batch: FileBatch): FileBatch
    fun findByIdWithOrders(id: UUID): FileBatch?
    fun findByWindowAndDate(window: SettlementWindow, date: LocalDate): List<FileBatch>
    fun updateStatus(batch: FileBatch): FileBatch
    fun existsActiveBatch(window: SettlementWindow, date: LocalDate, participantId: UUID): Boolean
}