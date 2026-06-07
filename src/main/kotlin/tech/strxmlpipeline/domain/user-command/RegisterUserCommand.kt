package tech.strxmlpipeline.domain.`user-command`

import java.util.UUID

data class RegisterUserCommand(
    val name: String,
    val password: String,
    val roleId: UUID
)
