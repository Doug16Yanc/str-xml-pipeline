package tech.strxmlpipeline.web.dto.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token must not be blank")
    val refreshToken: String,
)
