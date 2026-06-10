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

@Component
class JwtAuthenticationFilter(
    private val tokenGenerator: TokenGeneratorPort,
    private val userRepository: UserRepositoryPort,
    private val tokenBlacklist: TokenBlacklistPort
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

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
                logger.warn("Redis indisponível — assumindo token não revogado: ${ex.message}")
                false
            }

            if (revoked) {
                chain.doFilter(request, response)
                return
            }

            val userId = tokenGenerator.validateAccessToken(token)
            if (userId != null) {
                val user = userRepository.findById(userId)
                if (user != null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority(user.role.roleType.name))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }

        chain.doFilter(request, response)
    }
}