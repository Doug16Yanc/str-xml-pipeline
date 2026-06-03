package tech.strxmlpipeline.infrastructure.persistence.adapter

import tech.strxmlpipeline.domain.model.Ispb
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.ParticipantJpaRepository
import org.springframework.stereotype.Component

@Component
class ParticipantRepositoryAdapter(
    private val jpaRepository: ParticipantJpaRepository
) : ParticipantPort {

    override fun findByIspb(ispb: Ispb): Participant? {
        return jpaRepository.findByIspb(ispb.value)?.toDomain()
    }

    override fun save(participant: Participant): Participant {
        val entity = participant.toEntity()
        return jpaRepository.save(entity).toDomain()
    }
}