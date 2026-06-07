package tech.strxmlpipeline.domain.`user-command`

import java.util.UUID

data class RefreshTokenCommand(
    val userId: UUID,
    val rawRefreshToken: String
)
