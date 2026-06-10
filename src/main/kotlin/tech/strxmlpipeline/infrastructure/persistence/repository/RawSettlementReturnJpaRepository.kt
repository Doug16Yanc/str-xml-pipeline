package tech.strxmlpipeline.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import tech.strxmlpipeline.infrastructure.persistence.entity.RawSettlementReturnEntity
import java.util.UUID

@Repository
interface RawSettlementReturnJpaRepository : JpaRepository<RawSettlementReturnEntity, UUID> {

    @Modifying
    @Query("UPDATE RawSettlementReturnEntity r SET r.batchId = :batchId WHERE r.id = :id")
    fun linkToBatch(@Param("id") id: UUID, @Param("batchId") batchId: UUID): Int
}
