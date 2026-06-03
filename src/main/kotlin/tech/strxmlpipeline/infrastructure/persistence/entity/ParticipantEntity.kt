package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "participant",
    indexes = [
        Index(name = "idx_participant_ispb", columnList = "ispn")
    ]
)
class ParticipantEntity(

    @Id
    @Column(name = "id", updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "ispb", length = 8, nullable = false, updatable = false)
    val ispb: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "type", length = 30, nullable = false)
    val type: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()

)