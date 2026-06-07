package tech.strxmlpipeline.domain.model

import java.util.UUID
import java.time.Instant

data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val passwordHash: String,
    val role: Role,
    val createdAt: Instant = Instant.now()
)
