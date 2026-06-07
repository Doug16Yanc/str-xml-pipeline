package tech.strxmlpipeline.infrastructure.security.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimitBucketRegistry(
    private val props: RateLimitProperties,
    private val redisClient: RedisClient,
) {
    private val connection =
        redisClient.connect(ByteArrayCodec.INSTANCE)

    @Suppress("DEPRECATION")
    private val proxyManager: ProxyManager<ByteArray> =
        LettuceBasedProxyManager.builderFor(connection)
            .build()

    fun resolveBucket(ip: String, endpoint: String) =
        proxyManager
            .builder()
            .build("ratelimit:$ip::$endpoint".toByteArray()) { buildConfiguration(endpoint) }

    private fun buildConfiguration(endpoint: String): BucketConfiguration {
        val bandwidth = when {
            endpoint.contains("login") -> Bandwidth.builder()
                .capacity(props.loginCapacity)
                .refillGreedy(props.loginRefillTokens, Duration.ofSeconds(props.loginRefillSeconds))
                .build()

            endpoint.contains("register") -> Bandwidth.builder()
                .capacity(props.registerCapacity)
                .refillGreedy(props.registerRefillTokens, Duration.ofSeconds(props.registerRefillSeconds))
                .build()

            endpoint.contains("refresh") -> Bandwidth.builder()
                .capacity(props.refreshCapacity)
                .refillGreedy(props.refreshRefillTokens, Duration.ofSeconds(props.refreshRefillSeconds))
                .build()

            else -> Bandwidth.builder()
                .capacity(20)
                .refillGreedy(20, Duration.ofSeconds(60))
                .build()
        }
        return BucketConfiguration.builder().addLimit(bandwidth).build()
    }
}