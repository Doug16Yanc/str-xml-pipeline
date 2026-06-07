package tech.strxmlpipeline.infrastructure.security.ratelimit

import io.github.bucket4j.ConsumptionProbe
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter(
    private val registry: RateLimitBucketRegistry,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    private val rateLimitedPaths = setOf(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI !in rateLimitedPaths

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val ip       = resolveClientIp(request)
        val endpoint = request.requestURI
        val bucket   = registry.resolveBucket(ip, endpoint)

        val probe: ConsumptionProbe = bucket.tryConsumeAndReturnRemaining(1)

        response.setHeader("X-RateLimit-Remaining", probe.remainingTokens.toString())
        response.setHeader("X-RateLimit-Retry-After-Seconds",
            if (!probe.isConsumed)
                TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill).toString()
            else "0"
        )

        if (!probe.isConsumed) {
            log.warn("Rate limit exceeded — IP: $ip | endpoint: $endpoint")
            rejectRequest(response, probe)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveClientIp(request: HttpServletRequest): String {
        val headersToCheck = listOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "X-Original-Forwarded-For",
        )
        for (header in headersToCheck) {
            val value = request.getHeader(header)
            if (!value.isNullOrBlank() && value != "unknown") {
                return value.split(",").first().trim()
            }
        }
        return request.remoteAddr
    }

    private fun rejectRequest(response: HttpServletResponse, probe: ConsumptionProbe) {
        val retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill)
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.setHeader("Retry-After", retryAfter.toString())
        response.writer.write(
            """
            {
              "status": 429,
              "error": "Too Many Requests",
              "message": "Limit of attempts exceeded. Please try again at ${retryAfter}s.",
              "retryAfterSeconds": $retryAfter
            }
            """.trimIndent()
        )
    }
}
