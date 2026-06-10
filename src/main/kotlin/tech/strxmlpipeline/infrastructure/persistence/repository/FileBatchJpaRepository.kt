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
        SELECT b FROM FileBatchEntity b
        WHERE b.window = :window
        AND b.referenceDate = :date
    """)
    fun findByWindowAndReferenceDate(
        @Param("window") window: String,
        @Param("date") date: LocalDate,
    ): List<FileBatchEntity>

    /**
     * Prevents duplicate active batches for the same window+date.
     * Called by the scheduler before assembly to detect race conditions
     * (e.g. double-fire due to clock skew or manual trigger).
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM FileBatchEntity b
        WHERE b.window = :window
        AND b.referenceDate = :date
        AND b.status NOT IN (
            tech.strxmlpipeline.domain.enum.BatchStatus.REJECTED
        )
    """)
    fun existsActiveBatchForWindowAndDate(
        @Param("window") window: String,
        @Param("date") date: LocalDate,
    ): Boolean

}