package tech.strxmlpipeline.infrastructure.persistence.adapter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.out.RefreshTokenPort
import java.util.concurrent.TimeUnit
import java.util.UUID

@Component
class RefreshTokenRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : RefreshTokenPort {

    companion object {
        private const val PREFIX = "refresh_token:"
    }

    override fun save(userId: UUID, rawToken: String, ttlMs: Long) {
        redisTemplate.opsForValue().set(
            "$PREFIX$userId",
            rawToken,
            ttlMs,
            TimeUnit.MILLISECONDS,
        )
    }

    override fun validate(userId: UUID, rawToken: String): Boolean {
        val stored = redisTemplate.opsForValue().get("$PREFIX$userId")
        return stored != null && stored == rawToken
    }

    override fun revoke(userId: UUID) {
        redisTemplate.delete("$PREFIX$userId")
    }
}