package tech.strxmlpipeline.infrastructure.persistence.adapter

import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.port.out.UserRepositoryPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toUserDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toUserEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.UserJpaRepository
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val jpaRepository: UserJpaRepository
) : UserRepositoryPort {

    override fun save(user: User): User {
        val entity = user.toUserEntity()
        val saved = jpaRepository.save(entity)
        return saved.toUserDomain()
    }

    override fun findById(id: UUID): User? =
        jpaRepository.findById(id)
            .map { it.toUserDomain() }
            .orElse(null)

    override fun findByName(name: String): User? {
        return jpaRepository.findByName(name)?.toUserDomain()
    }

    override fun existsByName(name: String): Boolean {
        if (name.isBlank()) return false
        return jpaRepository.existsByNameIgnoreCase(name.trim())
    }
}