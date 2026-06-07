package tech.strxmlpipeline.web.controller.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.domain.port.`in`.LoginUseCase
import tech.strxmlpipeline.domain.port.`in`.LogoutUseCase
import tech.strxmlpipeline.domain.port.`in`.RefreshUseCase
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort
import tech.strxmlpipeline.domain.`user-command`.LoginCommand
import tech.strxmlpipeline.domain.`user-command`.RefreshTokenCommand
import tech.strxmlpipeline.domain.valueobject.OperatorName
import tech.strxmlpipeline.infrastructure.exception.local.InvalidTokenException
import tech.strxmlpipeline.web.dto.auth.LoginRequest
import tech.strxmlpipeline.web.dto.auth.LoginResponse
import tech.strxmlpipeline.web.dto.auth.TokenResponse

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshUseCase: RefreshUseCase,
    private val tokenGenerator: TokenGeneratorPort,
    @Value("\${security.jwt.access-token-expiry-ms:3600000}") private val accessTokenExpiryMs: Long,
    @Value("\${security.jwt.refresh-token-expiry-ms:604800000}") private val refreshTokenExpiryMs: Long,
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<LoginResponse> {
        val result = loginUseCase.login(
            LoginCommand(name = request.name, password = request.password)
        )

        setAccessTokenCookie(response, result.tokens.accessToken, accessTokenExpiryMs)
        setRefreshTokenCookie(response, result.tokens.refreshToken, refreshTokenExpiryMs)

        return ResponseEntity.ok(
            LoginResponse(
                expiresIn = result.tokens.expiresIn,
                userId    = result.user.id,
                name      = result.user.name,
                role      = result.user.role.roleType,
                ispb      = runCatching {
                    OperatorName(result.user.name).ispb
                }.getOrNull(),
            )
        )
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val accessToken = extractCookie(request, "jwt")

        if (accessToken != null) {
            logoutUseCase.execute(accessToken)
        }

        clearCookie(response, "jwt")
        clearCookie(response, "jwt-refresh")

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<TokenResponse> {
        val refreshToken = extractCookie(request, "jwt-refresh")
            ?: throw InvalidTokenException("Refresh token cookie not found")

        val accessToken = extractCookie(request, "jwt")
        val userId = accessToken
            ?.let { runCatching { tokenGenerator.extractUserIdIgnoringExpiry(it) }.getOrNull() }
            ?: throw InvalidTokenException("Cannot resolve user from access token")

        val pair = refreshUseCase.refresh(
            RefreshTokenCommand(userId = userId, rawRefreshToken = refreshToken)
        )

        setAccessTokenCookie(response, pair.accessToken, accessTokenExpiryMs)
        setRefreshTokenCookie(response, pair.refreshToken, refreshTokenExpiryMs)

        return ResponseEntity.ok(TokenResponse.Companion.from(pair))
    }

    private fun setAccessTokenCookie(response: HttpServletResponse, token: String, ttlMs: Long) {
        Cookie("jwt", token).apply {
            isHttpOnly = true
            secure     = true
            path       = "/"
            maxAge     = (ttlMs / 1000).toInt()
            setAttribute("SameSite", "Strict")
        }.also { response.addCookie(it) }
    }

    private fun setRefreshTokenCookie(response: HttpServletResponse, token: String, ttlMs: Long) {
        Cookie("jwt-refresh", token).apply {
            isHttpOnly = true
            secure     = true
            path       = "/v1/auth/refresh"
            maxAge     = (ttlMs / 1000).toInt()
            setAttribute("SameSite", "Strict")
        }.also { response.addCookie(it) }
    }

    private fun clearCookie(response: HttpServletResponse, name: String) {
        Cookie(name, "").apply {
            isHttpOnly = true
            secure     = true
            path       = "/"
            maxAge     = 0
        }.also { response.addCookie(it) }
    }

    private fun extractCookie(request: HttpServletRequest, name: String): String? =
        request.cookies?.find { it.name == name }?.value
}