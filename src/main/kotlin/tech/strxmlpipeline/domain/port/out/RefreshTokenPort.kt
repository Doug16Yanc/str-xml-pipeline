package tech.strxmlpipeline.domain.port.out

import java.util.UUID

interface RefreshTokenPort {
    fun save(userId: UUID, rawToken: String, ttlMs: Long)
    fun validate(userId: UUID, rawToken: String): Boolean
    fun revoke(userId: UUID)
}