package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.model.Role
import tech.strxmlpipeline.domain.port.out.RoleRepositoryPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.RoleJpaRepository
import java.util.UUID

@Component
@Transactional
class RolePersistenceAdapter(
    private val jpaRepository: RoleJpaRepository
) : RoleRepositoryPort {

    override fun save(role: Role): Role {
        val entity = role.toEntity()
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun fndById(id: UUID): Role? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByType(type: RoleType): Role? {
        return jpaRepository.findByRoleType(type)
            ?.toDomain()
    }
}