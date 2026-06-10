package tech.strxmlpipeline.web.dto.request

import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.domain.model.Participant
import tech.strxmlpipeline.domain.valueobject.Ispb

data class CreateParticipantRequest(
    val ispb: String,
    val name: String,
    val account: String?,
    val branch: String?,
) {
    fun toDomain(type: ParticipantType) = Participant(
        ispb = Ispb(ispb),
        name = name,
        type = type,
        account = account,
        branch = branch,
    )
}
