package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.FileBatchEntity
import java.time.LocalDate
import java.util.UUID

@Repository
interface FileBatchJpaRepository : JpaRepository<FileBatchEntity, UUID> {

    @Query("""
        SELECT f FROM FileBatchEntity f 
        LEFT JOIN FETCH f.orders 
        WHERE f.id = :id
    """)
    fun findByIdWithOrders(@Param("id") id: UUID): FileBatchEntity?

    @Query("""
        SELECT b FROM FileBatchEntity b
        WHERE b.window = :window
        AND b.referenceDate = :date
    """)
    fun findByWindowAndReferenceDate(
        @Param("window") window: String,
        @Param("date") date: LocalDate,
    ): List<FileBatchEntity>

    @Query("""
    SELECT COUNT(b) > 0 FROM FileBatchEntity b
    WHERE b.window = :window
    AND b.referenceDate = :date
    AND b.participant.id = :participantId
    AND b.status NOT IN (
        tech.strxmlpipeline.domain.enum.BatchStatus.REJECTED
    )
""")
    fun existsActiveBatchForWindowDateAndParticipant(
        @Param("window") window: String,
        @Param("date") date: LocalDate,
        @Param("participantId") participantId: UUID,
    ): Boolean

}