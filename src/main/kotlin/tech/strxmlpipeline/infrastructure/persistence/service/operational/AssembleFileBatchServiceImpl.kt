package tech.strxmlpipeline.infrastructure.persistence.service.operational

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.`in`.AssembleFileBatchUseCase
import tech.strxmlpipeline.domain.port.out.FileBatchPort
import tech.strxmlpipeline.domain.port.out.FileBatchPublisherPort
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.infrastructure.persistence.processor.ParticipantBatchProcessor
import java.time.Clock
import java.time.LocalDate

@Service
class AssembleFileBatchServiceImpl(
    private val participantPort: ParticipantPort,
    private val processor: ParticipantBatchProcessor,
    private val clock: Clock,
    @PersistenceContext private val entityManager: EntityManager,
) : AssembleFileBatchUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun assemble(windowKey: String) {
        val window = SettlementWindow.parse(windowKey)
        val today  = LocalDate.now(clock)

        entityManager.clear()

        val participants = participantPort.findAll()
            .filter { it.type != ParticipantType.BACEN }

        if (participants.isEmpty()) {
            log.info("No participants found for window [{}]", windowKey)
            return
        }

        participants.forEach { participant ->
            try {
                processor.process(window, today, participant)
            } catch (e: Exception) {
                log.error("Erro crítico ao processar o participante [{}]. Pulando para o próximo.", participant.ispb, e)
            }
        }
    }
}