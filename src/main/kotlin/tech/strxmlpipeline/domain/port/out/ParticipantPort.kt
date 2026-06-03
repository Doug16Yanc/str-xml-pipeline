package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.Ispb
import tech.strxmlpipeline.domain.model.Participant

interface ParticipantPort {
    fun findByIspb(ispb: Ispb): Participant?
    fun save(participant: Participant): Participant
}