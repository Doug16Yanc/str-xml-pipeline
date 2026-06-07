package tech.strxmlpipeline.infrastructure.persistence.service.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.port.`in`.LogoutUseCase
import tech.strxmlpipeline.domain.port.out.RefreshTokenPort
import tech.strxmlpipeline.domain.port.out.TokenBlacklistPort
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort

@Service
class LogoutService(
    private val tokenGenerator: TokenGeneratorPort,
    private val tokenBlacklist: TokenBlacklistPort,
    private val refreshTokenPort: RefreshTokenPort,
) : LogoutUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(accessToken: String) {
        val ttl = tokenGenerator.getRemainingTtlMs(accessToken)
        if (ttl > 0) {
            tokenBlacklist.revoke(accessToken, ttl)
        }

        val userId = tokenGenerator.extractUserIdIgnoringExpiry(accessToken)
        if (userId != null) {
            refreshTokenPort.revoke(userId)
            log.info("Logout — user [{}] tokens revoked", userId)
        }
    }
}