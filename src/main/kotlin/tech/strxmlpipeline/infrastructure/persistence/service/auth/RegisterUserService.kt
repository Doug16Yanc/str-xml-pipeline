package tech.strxmlpipeline.infrastructure.persistence.service.auth

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.port.`in`.RegisterUserUseCase
import tech.strxmlpipeline.domain.port.out.PasswordHasherPort
import tech.strxmlpipeline.domain.port.out.RoleRepositoryPort
import tech.strxmlpipeline.domain.port.out.UserRepositoryPort
import tech.strxmlpipeline.domain.`user-command`.RegisterUserCommand
import tech.strxmlpipeline.infrastructure.exception.local.RoleNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.UserAlreadyExistsException

@Service
@Transactional
class RegisterUserService(
    private val userPort: UserRepositoryPort,
    private val rolePort: RoleRepositoryPort,
    private val passwordHasher: PasswordHasherPort,
) : RegisterUserUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun register(command: RegisterUserCommand): User {
        if (userPort.existsByName(command.name)) {
            throw UserAlreadyExistsException(command.name)
        }

        val role = rolePort.findById(command.roleId)
            ?: throw RoleNotFoundException(command.roleId)

        val user = User(
            name = command.name,
            passwordHash = passwordHasher.hash(command.password),
            role = role,
            ispb = command.extractIspb()
        )

        return userPort.save(user).also {
            log.info("User registered — name [{}] role [{}]", it.name, role.roleType)
        }
    }
}
