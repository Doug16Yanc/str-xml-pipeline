package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import tech.strxmlpipeline.infrastructure.persistence.entity.UserEntity
import java.util.UUID

interface UserJpaRepository: JpaRepository<UserEntity, UUID> {
    fun findByName(name: String): UserEntity?
    fun existsByNameIgnoreCase(name: String): Boolean
}