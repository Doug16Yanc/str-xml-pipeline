package tech.strxmlpipeline.web.dto.response

import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.domain.model.Participant

data class ParticipantResponse(
    val ispb: String,
    val name: String,
    val type: ParticipantType,
    val account: String?,
    val branch: String?,
) {
    companion object {
        fun from(p: Participant) = ParticipantResponse(
            ispb    = p.ispb.value,
            name    = p.name,
            type    = p.type,
            account = p.account,
            branch  = p.branch,
        )
    }
}