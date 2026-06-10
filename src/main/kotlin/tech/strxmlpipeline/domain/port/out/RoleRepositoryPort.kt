package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.model.Role
import java.util.UUID

interface RoleRepositoryPort {
    fun findById(id: UUID): Role?
    fun findByType(type: RoleType): Role?
}