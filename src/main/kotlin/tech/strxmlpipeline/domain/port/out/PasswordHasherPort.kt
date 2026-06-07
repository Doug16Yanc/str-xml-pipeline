package tech.strxmlpipeline.domain.port.out

interface PasswordHasherPort {
    fun hash(raw: String): String
    fun matches(raw: String, hash: String): Boolean
}