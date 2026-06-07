package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.User
import java.util.UUID

interface UserRepositoryPort {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByName(name: String): User?
    fun existsByName(name: String): Boolean
}