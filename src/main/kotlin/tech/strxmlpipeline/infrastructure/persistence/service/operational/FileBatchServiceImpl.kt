package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.infrastructure.exception.local.FileBatchNotFoundException
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FileBatchServiceImpl(
    private val batchPort: FileBatchPort,
) {

    fun findById(id: UUID): FileBatch =
        batchPort.findById(id)
            ?: throw FileBatchNotFoundException(id)

    fun findByWindowAndDate(windowKey: String, date: LocalDate): List<FileBatch> {
        val window = SettlementWindow.parse(windowKey)
        return batchPort.findByWindowAndDate(window, date)
    }
}

