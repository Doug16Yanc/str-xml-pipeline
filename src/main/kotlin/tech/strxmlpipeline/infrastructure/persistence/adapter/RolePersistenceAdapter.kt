package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.model.Role
import tech.strxmlpipeline.domain.port.out.RoleRepositoryPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toRoleDomain
import tech.strxmlpipeline.infrastructure.persistence.repository.RoleJpaRepository
import java.util.UUID

@Component
@Transactional
class RolePersistenceAdapter(
    private val jpaRepository: RoleJpaRepository
) : RoleRepositoryPort {

    override fun findById(id: UUID): Role? {
        return jpaRepository.findById(id)
            .map { it.toRoleDomain() }
            .orElse(null)
    }

    override fun findByType(type: RoleType): Role? {
        return jpaRepository.findByRoleType(type)
            ?.toRoleDomain()
    }
}