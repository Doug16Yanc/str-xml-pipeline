package tech.strxmlpipeline.domain.port.`in`

import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.`user-command`.RegisterUserCommand

interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): User
}