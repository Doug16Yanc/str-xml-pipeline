package tech.strxmlpipeline.infrastructure.persistence.service.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.port.`in`.LoginUseCase
import tech.strxmlpipeline.domain.port.out.PasswordHasherPort
import tech.strxmlpipeline.domain.port.out.RefreshTokenPort
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort
import tech.strxmlpipeline.domain.port.out.UserRepositoryPort
import tech.strxmlpipeline.domain.`user-command`.LoginCommand
import tech.strxmlpipeline.domain.valueobject.LoginResult
import tech.strxmlpipeline.domain.valueobject.TokenPair
import tech.strxmlpipeline.infrastructure.exception.local.InvalidCredentialsException


@Service
@Transactional(readOnly = true)
class LoginService(
    private val userPort: UserRepositoryPort,
    private val passwordHasher: PasswordHasherPort,
    private val tokenGenerator: TokenGeneratorPort,
    private val refreshTokenPort: RefreshTokenPort,
    @Value("\${security.jwt.refresh-token-expiry-ms:604800000}")
    private val refreshTokenTtlMs: Long,
) : LoginUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun login(command: LoginCommand): LoginResult {
        val user = userPort.findByName(command.name)
            ?: throw InvalidCredentialsException()

        if (!passwordHasher.matches(command.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val accessToken  = tokenGenerator.generateAccessToken(user)
        val refreshToken = tokenGenerator.generateRefreshToken()

        refreshTokenPort.save(user.id, refreshToken, refreshTokenTtlMs)

        log.info("Login successful — user [{}] role [{}]", user.name, user.role.roleType)

        return LoginResult(
            tokens = TokenPair(
                accessToken  = accessToken,
                refreshToken = refreshToken,
                expiresIn    = refreshTokenTtlMs,
            ),
            user = user,
        )
    }
}
