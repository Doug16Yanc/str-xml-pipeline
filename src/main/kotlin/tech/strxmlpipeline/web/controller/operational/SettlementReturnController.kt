package tech.strxmlpipeline.web.controller.operational

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.infrastructure.persistence.service.operational.SettlementReturnServiceImpl
import tech.strxmlpipeline.web.dto.response.SettlementReturnResponse

import java.util.UUID

@RestController
@RequestMapping("/v1/returns")
class SettlementReturnController(
    private val returnService: SettlementReturnServiceImpl,
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<SettlementReturnResponse> =
        ResponseEntity.ok(
            SettlementReturnResponse.from(returnService.findById(id))
        )

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByBatchId(@PathVariable batchId: UUID): ResponseEntity<SettlementReturnResponse> =
        ResponseEntity.ok(
            SettlementReturnResponse.from(returnService.findByBatchId(batchId))
        )
}