package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.CharJdbcType
import tech.strxmlpipeline.domain.enum.ParticipantType
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "participant",
    indexes = [
        Index(name = "idx_participant_ispb", columnList = "ispb")
    ]
)
class ParticipantEntity(

    @Id
    @Column(name = "id", updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "ispb", columnDefinition = "char(8)", nullable = false)
    @JdbcType(CharJdbcType::class)
    val ispb: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    val type: ParticipantType,

    @Column(name = "account", length = 20)
    val account: String? = null,

    @Column(name = "branch", length = 4)
    val branch: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()

)