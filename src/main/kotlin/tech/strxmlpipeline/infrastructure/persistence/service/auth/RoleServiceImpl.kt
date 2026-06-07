package tech.strxmlpipeline.infrastructure.persistence.service.auth

import org.springframework.stereotype.Service
import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.model.Role
import tech.strxmlpipeline.domain.port.`in`.ManageRoleUseCase
import tech.strxmlpipeline.domain.port.out.RoleRepositoryPort
import tech.strxmlpipeline.infrastructure.exception.local.RoleNotFoundException

@Service
class RoleServiceImpl(
    private val roleRepository: RoleRepositoryPort
) : ManageRoleUseCase {

    override fun findByType(type: RoleType): Role {
        return roleRepository.findByType(type)
            ?: throw RoleNotFoundException(type)
    }

    override fun createRole(type: RoleType): Role {
        val friendlyDescription = type.name
            .lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }

        val newRole = Role(
            roleType = type,
            description = "$friendlyDescription profile on the system.",
        )

        return roleRepository.save(newRole)
    }
}