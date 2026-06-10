package tech.strxmlpipeline.infrastructure.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tech.strxmlpipeline.domain.port.out.TokenBlacklistPort
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort
import tech.strxmlpipeline.domain.port.out.UserRepositoryPort
import tech.strxmlpipeline.infrastructure.exception.local.InvalidTokenException
import tech.strxmlpipeline.infrastructure.exception.local.TokenExpiredException

@Component
class JwtAuthenticationFilter(
    private val tokenGenerator: TokenGeneratorPort,
    private val userRepository: UserRepositoryPort,
    private val tokenBlacklist: TokenBlacklistPort,
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI in PUBLIC_URIS

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val token = request.cookies
            ?.firstOrNull { it.name == "jwt" }
            ?.value

        if (token != null) {
            val revoked = try {
                tokenBlacklist.isRevoked(token)
            } catch (ex: Exception) {
                logger.warn("Redis unavailable — assuming token not revoked: ${ex.message}")
                false
            }

            if (revoked) {
                chain.doFilter(request, response)
                return
            }

            val userId = try {
                tokenGenerator.validateAccessToken(token)
            } catch (ex: TokenExpiredException) {
                logger.debug("Expired token on [{}] — continuing unauthenticated", request.requestURI)
                null
            } catch (ex: InvalidTokenException) {
                logger.debug("Invalid token on [{}] — continuing unauthenticated", request.requestURI)
                null
            }

            if (userId != null) {
                val user = userRepository.findById(userId)
                if (user != null) {
                    val ispb = runCatching {
                        user.name.split("_").firstOrNull()
                    }.getOrNull()

                    val auth = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority(user.role.roleType.name))
                    ).also {
                        it.details = mapOf("ispb" to ispb, "name" to user.name)
                    }

                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }

        chain.doFilter(request, response)
    }

    companion object {
        private val PUBLIC_URIS = setOf(
            "/v1/auth/login",
            "/v1/auth/refresh",
            "/v1/users",
            "/actuator/health",
            "/actuator/info",
        )
    }
}