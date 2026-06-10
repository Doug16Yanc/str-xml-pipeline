package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.infrastructure.exception.local.ParticipantNotFoundException

@Service
@Transactional(readOnly = true)
class ParticipantServiceImpl(
    private val participantPort: ParticipantPort,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun findByIspb(ispb: String): Participant =
        participantPort.findByIspb(Ispb(ispb))
            ?: throw ParticipantNotFoundException("Participant not found for ISPB: $ispb")

    fun findAll(): List<Participant> =
        participantPort.findAll()

    @Transactional
    fun create(participant: Participant): Participant {
        val existing = participantPort.findByIspb(participant.ispb)
        check(existing == null) {
            "Participant already registered for ISPB: ${participant.ispb}"
        }

        return participantPort.save(participant).also {
            log.info("Participant registered — ISPB [{}] name [{}]", it.ispb.value, it.name)
        }
    }

    @Transactional
    fun update(participant: Participant): Participant {
        participantPort.findByIspb(participant.ispb)
            ?: throw ParticipantNotFoundException("Participant not found for ISPB: ${participant.ispb.value}")

        return participantPort.save(participant).also {
            log.info("Participant updated — ISPB [{}]", participant.ispb.value)
        }
    }
}
