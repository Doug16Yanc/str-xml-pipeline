package tech.strxmlpipeline.infrastructure.security.jwt

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    var privateKeyPath: String = "",
    var publicKeyPath: String = "",
    var accessTokenExpiryMs: Long = 900_000,
) {
    @PostConstruct
    fun validate() {
        require(privateKeyPath.isNotBlank()) { "jwt.private-key-path é obrigatório" }
        require(publicKeyPath.isNotBlank()) { "jwt.public-key-path é obrigatório" }
    }
}