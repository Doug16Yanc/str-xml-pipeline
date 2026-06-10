package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.domain.valueobject.Ispb
import java.util.UUID

data class Participant(
    val id: UUID = UUID.randomUUID(),
    val ispb: Ispb,
    val name: String,
    val type: ParticipantType,
    val account: String?,
    val branch: String?
)

