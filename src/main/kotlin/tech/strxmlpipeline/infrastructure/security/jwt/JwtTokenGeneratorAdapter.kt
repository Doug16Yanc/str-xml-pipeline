package tech.strxmlpipeline.infrastructure.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.port.out.TokenGeneratorPort
import java.util.Date
import java.util.UUID

@Component
class JwtTokenGeneratorAdapter(
    private val props: JwtProperties,
    private val keyLoader: JwtKeyLoader,
) : TokenGeneratorPort {

    override fun generateAccessToken(user: User): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("role", user.role.roleType)
            .claim("ispb", user.ispb)
            .claim("name", user.name.value)
            .issuedAt(Date(now))
            .expiration(Date(now + props.accessTokenExpiryMs))
            .signWith(keyLoader.privateKey(), Jwts.SIG.ES384)
            .compact()
    }
    override fun generateRefreshToken(): String = UUID.randomUUID().toString()

    override fun validateAccessToken(token: String): UUID? =
        runCatching {
            val claims = Jwts.parser()
                .verifyWith(keyLoader.publicKey())
                .build()
                .parseSignedClaims(token)
                .payload
            UUID.fromString(claims.subject)
        }.getOrElse { ex ->
            when (ex) {
                is ExpiredJwtException -> throw TokenExpiredException()
                is JwtException -> throw InvalidTokenException()
                else -> throw ex
            }
        }

    override fun getRemainingTtlMs(token: String): Long {
        return runCatching {
            val claims = Jwts.parser()
                .verifyWith(keyLoader.publicKey())
                .build()
                .parseSignedClaims(token)
                .payload
            val expiresAt = claims.expiration.time
            maxOf(0L, expiresAt - System.currentTimeMillis())
        }.getOrDefault(0L)
    }

    override fun extractUserIdIgnoringExpiry(token: String): UUID? =
        runCatching {
            val claims = Jwts.parser()
                .verifyWith(keyLoader.publicKey())
                .clockSkewSeconds(Long.MAX_VALUE / 1000)
                .build()
                .parseSignedClaims(token)
                .payload
            UUID.fromString(claims.subject)
        }.getOrNull()
}