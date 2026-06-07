package tech.strxmlpipeline.web.dto.auth

import tech.strxmlpipeline.domain.enum.RoleType
import java.util.UUID

data class RegisterUserResponse (
    val id: UUID,
    val name: String,
    val role: RoleType
)