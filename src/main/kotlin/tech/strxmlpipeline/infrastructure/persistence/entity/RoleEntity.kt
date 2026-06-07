package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import tech.strxmlpipeline.domain.enum.RoleType
import java.util.UUID

@Entity
@Table(name = "roles",
    indexes = [
        Index(name = "idx_roles_role_type", columnList = "role_type", unique = true)
    ]
)
class RoleEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    val roleType: RoleType,

    @Column(nullable = false)
    val description: String
)