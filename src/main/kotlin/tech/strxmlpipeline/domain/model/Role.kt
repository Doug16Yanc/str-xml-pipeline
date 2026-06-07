package tech.strxmlpipeline.domain.model

import java.util.UUID

data class Role(
    val id: UUID = UUID.randomUUID(),
    val roleType: RoleType,
    val description: String
)