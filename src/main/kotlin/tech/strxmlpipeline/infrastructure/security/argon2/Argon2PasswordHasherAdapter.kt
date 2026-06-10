package tech.strxmlpipeline.infra.security

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.out.PasswordHasherPort

@Component
class Argon2PasswordHasherAdapter(
    private val encoder: Argon2PasswordEncoder,
) : PasswordHasherPort {
    override fun hash(raw: String): String = encoder.encode(raw)!!
    override fun matches(raw: String, hash: String): Boolean = encoder.matches(raw, hash)
}