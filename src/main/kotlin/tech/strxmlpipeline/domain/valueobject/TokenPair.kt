package tech.strxmlpipeline.domain.valueobject

import tech.strxmlpipeline.domain.enum.RoleType
import java.util.UUID

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)