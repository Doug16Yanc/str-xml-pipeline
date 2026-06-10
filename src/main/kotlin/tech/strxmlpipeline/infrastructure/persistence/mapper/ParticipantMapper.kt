package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.infrastructure.persistence.entity.ParticipantEntity

fun ParticipantEntity.toParticipantDomain(): Participant = Participant(
    id = this.id,
    ispb = Ispb(this.ispb),
    name = this.name,
    type = this.type,
    account = this.account,
    branch = this.branch
)

fun Participant.toParticipantEntity(): ParticipantEntity = ParticipantEntity(
    id = this.id,
    ispb = this.ispb.value,
    name = this.name,
    type = this.type,
    account = this.account,
    branch = this.branch
)