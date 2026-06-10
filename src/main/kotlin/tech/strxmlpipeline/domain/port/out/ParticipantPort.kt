package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.valueobject.Ispb

interface ParticipantPort {
    fun findByIspb(ispb: Ispb): Participant?
    fun findAll(): List<Participant>
    fun save(participant: Participant): Participant
}