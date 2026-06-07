package tech.strxmlpipeline.infrastructure.config.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.StaticHeadersWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import tech.strxmlpipeline.infrastructure.security.jwt.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }

        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        .cors { it.configurationSource(corsConfigurationSource()) }

        .headers { headers ->
            headers.httpStrictTransportSecurity { hsts ->
                hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000)
            }
            headers.contentSecurityPolicy { csp ->
                csp.policyDirectives("default-src 'self'; frame-ancestors 'none'")
            }
            headers.frameOptions { it.deny() }
            headers.contentTypeOptions { }
            headers.cacheControl { }
            headers.addHeaderWriter(
                StaticHeadersWriter(
                    "Permissions-Policy",
                    "geolocation=(), microphone=(), camera=(), payment=()"
                )
            )
        }

        .authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/v1/users/create",
                "/v1/auth/login",
                "/v1/auth/logout",
                "/v1/auth/refresh",
                "/v1/actuator/health",
                "/v1/actuator/info",
            ).permitAll()

            auth.requestMatchers("/actuator/**").hasRole("ADMIN")

            auth.anyRequest().authenticated()
        }

        .exceptionHandling { exceptions ->
            exceptions.authenticationEntryPoint { _, response, authException ->
                response.contentType = "application/json"
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.write(
                    """{"status":401,"error":"Unauthorized","message":"${authException.message}"}"""
                )
            }
            exceptions.accessDeniedHandler { _, response, accessDeniedException ->
                response.contentType = "application/json"
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.writer.write(
                    """{"status":403,"error":"Forbidden","message":"${accessDeniedException.message}"}"""
                )
            }
        }

        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "https://rumintrack.tech",
                "https://app.rumintrack.tech",
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
            allowCredentials = true
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", config)
        }
    }

    @Bean
    fun passwordEncoder(): Argon2PasswordEncoder =
        Argon2PasswordEncoder(16, 32, 2, 65_536, 3)
}