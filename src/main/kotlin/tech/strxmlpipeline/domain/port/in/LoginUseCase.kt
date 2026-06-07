package tech.strxmlpipeline.domain.port.`in`

import tech.strxmlpipeline.domain.`user-command`.LoginCommand
import tech.strxmlpipeline.domain.`user-command`.RefreshTokenCommand
import tech.strxmlpipeline.domain.valueobject.LoginResult
import tech.strxmlpipeline.domain.valueobject.TokenPair
import java.util.UUID

interface LoginUseCase {
    fun login(command: LoginCommand): LoginResult
}