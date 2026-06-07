package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.valueobject.OperatorName
import tech.strxmlpipeline.infrastructure.persistence.entity.UserEntity

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = OperatorName(this.name),
    passwordHash = this.passwordHash,
    role = this.role.toDomain(),
    createdAt = this.createdAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name.value,
    passwordHash = this.passwordHash,
    role = this.role.toEntity(),
    createdAt = this.createdAt
)