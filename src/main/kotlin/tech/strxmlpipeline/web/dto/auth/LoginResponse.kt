package tech.strxmlpipeline.web.dto.auth

import tech.strxmlpipeline.domain.enum.RoleType
import java.util.UUID

data class LoginResponse(
    val expiresIn: Long,
    val userId: UUID,
    val name: String,
    val role: RoleType,
    val ispb: String?,
)