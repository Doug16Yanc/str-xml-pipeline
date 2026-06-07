package tech.strxmlpipeline.infrastructure.security.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
class RateLimitProperties(
    var loginCapacity: Long = 5,
    var loginRefillTokens: Long = 5,
    var loginRefillSeconds: Long = 60,
    var registerCapacity: Long = 3,
    var registerRefillTokens: Long = 3,
    var registerRefillSeconds: Long = 3600,
    var refreshCapacity: Long = 10,
    var refreshRefillTokens: Long = 10,
    var refreshRefillSeconds: Long = 60,
    var bucketCacheCleanupMinutes: Long = 10,
)