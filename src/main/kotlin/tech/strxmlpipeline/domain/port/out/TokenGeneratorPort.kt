package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.User
import java.util.UUID

interface TokenGeneratorPort {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(): String
    fun validateAccessToken(token: String): UUID?
    fun getRemainingTtlMs(token: String): Long
    fun extractUserIdIgnoringExpiry(token: String): UUID?
}