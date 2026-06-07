package tech.strxmlpipeline.infrastructure.persistence.service.auth

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.port.`in`.RefreshUseCase
import tech.strxmlpipeline.domain.port.out.RefreshTokenPort
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort
import tech.strxmlpipeline.domain.port.out.UserRepositoryPort
import tech.strxmlpipeline.domain.`user-command`.RefreshTokenCommand
import tech.strxmlpipeline.domain.valueobject.TokenPair
import tech.strxmlpipeline.infrastructure.exception.local.InvalidTokenException


@Service
@Transactional
class RefreshService(
    private val userPort: UserRepositoryPort,
    private val tokenGenerator: TokenGeneratorPort,
    private val refreshTokenPort: RefreshTokenPort,
    @Value("\${security.jwt.refresh-token-expiry-ms:604800000}")
    private val refreshTokenTtlMs: Long,
) : RefreshUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun refresh(command: RefreshTokenCommand): TokenPair {
        val user = userPort.findById(command.userId)
            ?: throw InvalidTokenException("User not found for refresh")

        if (!refreshTokenPort.validate(command.userId, command.rawRefreshToken)) {
            throw InvalidTokenException("Invalid or expired refresh token")
        }

        refreshTokenPort.revoke(command.userId)

        val newAccessToken  = tokenGenerator.generateAccessToken(user)
        val newRefreshToken = tokenGenerator.generateRefreshToken()

        refreshTokenPort.save(user.id, newRefreshToken, refreshTokenTtlMs)

        log.info("Token refreshed — user [{}]", user.id)

        return TokenPair(accessToken = newAccessToken, refreshToken = newRefreshToken, expiresIn = refreshTokenTtlMs)
    }
}
