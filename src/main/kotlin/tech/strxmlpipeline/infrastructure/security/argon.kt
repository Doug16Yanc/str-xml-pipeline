package tech.strxmlpipeline.infrastructure.security

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import java.util.Base64

object Argon2Hasher {

    private const val ITERATIONS = 3
    private const val MEMORY_LIMIT_KB = 65536
    private const val PARALLELISM = 4
    private const val TAG_LENGTH = 32
    private const val SALT_LENGTH = 16

    fun hashPassword(password: String): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_LIMIT_KB)
            .withParallelism(PARALLELISM)
            .withSalt(salt)

        val generator = Argon2BytesGenerator()
        generator.init(builder.build())

        val result = ByteArray(TAG_LENGTH)
        generator.generateBytes(password.toByteArray(Charsets.UTF_8), result)

        val encoder = Base64.getEncoder()
        return "\$argon2id\$v=19\$m=$MEMORY_LIMIT_KB,t=$ITERATIONS,p=$PARALLELISM\$${encoder.encodeToString(salt)}\$${encoder.encodeToString(result)}"
    }
}