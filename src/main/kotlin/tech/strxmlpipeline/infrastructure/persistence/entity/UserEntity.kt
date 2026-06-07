package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users",
    indexes = [
        Index(name = "idx_users_name", columnList = "name", unique = true),
        Index(name = "idx_users_role_id", columnList = "role_id")
    ]
)
class UserEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val passwordHash: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    val role: RoleEntity,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)