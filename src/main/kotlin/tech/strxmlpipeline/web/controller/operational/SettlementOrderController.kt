package tech.strxmlpipeline.web.controller.operational

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.infrastructure.persistence.service.operational.ParticipantServiceImpl
import tech.strxmlpipeline.infrastructure.persistence.service.operational.SettlementOrderServiceImpl
import tech.strxmlpipeline.web.dto.request.CreateSettlementOrderRequest
import tech.strxmlpipeline.web.dto.response.SettlementOrderResponse
import java.util.UUID

@RestController
@RequestMapping("/v1/orders")
class SettlementOrderController(
    private val orderService: SettlementOrderServiceImpl,
    private val participantService: ParticipantServiceImpl,
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<SettlementOrderResponse> =
        ResponseEntity.ok(
            SettlementOrderResponse.from(orderService.findById(id))
        )

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByBatchId(@PathVariable batchId: UUID): ResponseEntity<List<SettlementOrderResponse>> =
        ResponseEntity.ok(
            orderService.findByBatchId(batchId).map { SettlementOrderResponse.from(it) }
        )

    @GetMapping("/find-by-status")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByStatus(
        @RequestParam status: OrderStatus,
    ): ResponseEntity<List<SettlementOrderResponse>> =
        ResponseEntity.ok(
            orderService.findByStatus(status).map { SettlementOrderResponse.from(it) }
        )

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SETTLEMENT_OPERATOR')")
    fun create(@RequestBody request: CreateSettlementOrderRequest): ResponseEntity<SettlementOrderResponse> {
        val originator  = participantService.findByIspb(request.originatorIspb)
        val destination = participantService.findByIspb(request.destinationIspb)
        val currentWindow = SettlementWindow.current()

        val order = orderService.create(
            order = request.toDomain(originator, destination),
            window = currentWindow
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SettlementOrderResponse.from(order))
    }
}