package tech.strxmlpipeline.domain.port.`in`

import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.model.Role

interface ManageRoleUseCase {
    fun createRole(type: RoleType): Role
    fun findByType(type: RoleType): Role
}