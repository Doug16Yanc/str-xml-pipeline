package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.Ispb
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.infrastructure.persistence.entity.ParticipantEntity

fun ParticipantEntity.toDomain(): Participant = Participant(
    ispb = Ispb(this.ispb),
    name = this.name,
    account = null,
    branch = null
)

fun Participant.toEntity(): ParticipantEntity = ParticipantEntity(
    ispb = this.ispb.value,
    name = this.name,
    type = "BANK"
)