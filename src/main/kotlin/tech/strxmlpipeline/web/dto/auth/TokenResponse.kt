package tech.strxmlpipeline.web.dto.auth

import tech.strxmlpipeline.domain.valueobject.TokenPair

data class TokenResponse(
    val expiresIn: Long,
) {
    companion object {
        fun from(pair: TokenPair) = TokenResponse(expiresIn = pair.expiresIn)
    }
}