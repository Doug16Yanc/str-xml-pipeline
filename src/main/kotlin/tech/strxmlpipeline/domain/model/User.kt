package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.valueobject.Ispb

import java.util.UUID
import java.time.Instant

data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val passwordHash: String,
    val role: Role,
    val ispb: Ispb? = null,
    val createdAt: Instant = Instant.now()
)
