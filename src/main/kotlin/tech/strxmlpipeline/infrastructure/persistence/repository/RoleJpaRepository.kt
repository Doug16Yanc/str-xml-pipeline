package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.infrastructure.persistence.entity.RoleEntity
import java.util.UUID

@Repository
interface RoleJpaRepository : JpaRepository<RoleEntity, UUID> {
    fun findByRoleType(roleType: RoleType): RoleEntity?
}