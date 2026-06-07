package tech.strxmlpipeline.domain.port.`in`

import tech.strxmlpipeline.domain.`user-command`.RefreshTokenCommand
import tech.strxmlpipeline.domain.valueobject.TokenPair

interface RefreshUseCase {
    fun refresh(command: RefreshTokenCommand): TokenPair
}
