package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.valueobject.Ispb

data class Participant(
    val ispb: Ispb,
    val name: String,
    val account: String?,
    val branch: String?
)

