package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.ParticipantEntity
import java.util.UUID

@Repository
interface ParticipantJpaRepository : JpaRepository<ParticipantEntity, UUID> {
    fun findByIspb(ispb: String): ParticipantEntity?
}