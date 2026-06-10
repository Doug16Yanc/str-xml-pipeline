package tech.strxmlpipeline.infrastructure.persistence.adapter

import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.infrastructure.persistence.repository.ParticipantJpaRepository
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.infrastructure.persistence.mapper.toParticipantDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toParticipantEntity

@Component
class ParticipantRepositoryAdapter(
    private val jpaRepository: ParticipantJpaRepository
) : ParticipantPort {

    override fun findByIspb(ispb: Ispb): Participant? {
        return jpaRepository.findByIspb(ispb.value)?.toParticipantDomain()
    }

    override fun findAll(): List<Participant> {
        return jpaRepository.findAll().map { it.toParticipantDomain() }
    }

    override fun save(participant: Participant): Participant {
        val entity = participant.toParticipantEntity()
        return jpaRepository.save(entity).toParticipantDomain()
    }
}