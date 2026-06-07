package tech.strxmlpipeline.infrastructure.security.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.out.TokenBlacklistPort
import java.util.concurrent.TimeUnit

@Component
class RedisTokenBlacklistAdapter(
    private val redis: StringRedisTemplate,
) : TokenBlacklistPort {

    override fun revoke(token: String, expiresInMs: Long) {

        if (expiresInMs <= 0) return

        val key = "blacklist:$token"
        redis.opsForValue().set(key, "revoked", expiresInMs, TimeUnit.MILLISECONDS)
    }

    override fun isRevoked(token: String): Boolean =
        redis.hasKey("blacklist:$token") == true
}