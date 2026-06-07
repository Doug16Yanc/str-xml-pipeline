package tech.strxmlpipeline.infrastructure.security.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class LettuceConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
    @Value("\${spring.data.redis.password:}") private val password: String,
) {

    @Bean(destroyMethod = "shutdown")
    fun redisClient(): RedisClient {
        val uri = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .apply { if (password.isNotBlank()) withPassword(password.toCharArray()) }
            .build()
        return RedisClient.create(uri)
    }

    @Bean
    @Primary
    fun redisConnectionFactory(redisClient: RedisClient): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration(host, port)
        if (password.isNotBlank()) config.setPassword(password)
        return LettuceConnectionFactory(config)
    }
}