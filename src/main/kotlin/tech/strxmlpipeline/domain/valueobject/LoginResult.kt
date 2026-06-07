package tech.strxmlpipeline.domain.valueobject

import tech.strxmlpipeline.domain.model.User

data class LoginResult(
    val tokens: TokenPair,
    val user: User
)