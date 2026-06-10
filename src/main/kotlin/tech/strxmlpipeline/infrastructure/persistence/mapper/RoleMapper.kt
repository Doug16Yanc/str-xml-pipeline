package tech.strxmlpipeline.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.Role
import tech.strxmlpipeline.infrastructure.persistence.entity.RoleEntity

fun RoleEntity.toRoleDomain(): Role = Role(
    id = this.id,
    roleType = this.roleType,
    description = this.description
)

fun Role.toRoleEntity(): RoleEntity = RoleEntity(
    id = this.id,
    roleType = this.roleType,
    description = this.description
)