package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.User
import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.domain.valueobject.OperatorName
import tech.strxmlpipeline.infrastructure.persistence.entity.UserEntity

fun UserEntity.toUserDomain(): User = User(
    id = this.id,
    name = name,
    passwordHash = this.passwordHash,
    role = this.role.toRoleDomain(),
    ispb = this.ispb?.let { Ispb(it) },
    createdAt = this.createdAt
)

fun User.toUserEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    passwordHash = this.passwordHash,
    role = this.role.toRoleEntity(),
    ispb = this.ispb?.value,
    createdAt = this.createdAt
)