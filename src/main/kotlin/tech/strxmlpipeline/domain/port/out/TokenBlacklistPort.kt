package tech.strxmlpipeline.domain.port.out

interface TokenBlacklistPort {
    fun revoke(token: String, expiresInMs: Long)
    fun isRevoked(token: String): Boolean
}