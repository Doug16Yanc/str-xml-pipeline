package tech.strxmlpipeline.infrastructure.config.app

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Clock

@Configuration
class AppConfig {

    /**
     * Single Clock bean — injected wherever time is needed.
     * Tests override with Clock.fixed(...) for deterministic cutoff validation.
     */
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    /**
     * Distributed scheduler lock.
     * Ensures that scheduled tasks execute on only one node at a time,
     * preventing duplicate XML generation, settlement processing, and
     * reconciliation jobs in a clustered deployment.
     */
    @Bean
    fun lockProvider(redisConnectionFactory: RedisConnectionFactory): LockProvider =
        RedisLockProvider(redisConnectionFactory)

    /**
     * Explicit ObjectMapper — do not rely on Spring Boot auto-configuration
     * for a pipeline that serializes financial messages. Every feature is
     * intentionally declared so behaviour doesn't change across Boot versions.
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        // Java time support — required for OffsetDateTime, LocalDate throughout the domain
        registerModule(JavaTimeModule())

        // Kotlin data class support — no-arg constructor inference, value classes
        registerModule(kotlinModule())

        // Serialize dates as ISO-8601 strings, not Unix timestamps
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        // STR return XML parsed into intermediate DTOs — unknown fields must not break parsing
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        // Fail loudly on null into primitives — catches mapping bugs early
        enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

        // Enums: unknown value becomes null instead of exception — defensive for new STR codes
        enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    }
}