package tech.strxmlpipeline.web.controller.operational

import tech.strxmlpipeline.infrastructure.persistence.service.operational.FileBatchServiceImpl

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.web.dto.response.FileBatchResponse
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/v1/batches")
class FileBatchController(
    private val batchService: FileBatchServiceImpl,
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<FileBatchResponse> =
        ResponseEntity.ok(
            FileBatchResponse.from(batchService.findById(id))
        )

    @GetMapping("find-by-window-and-date")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByWindowAndDate(
        @RequestParam windowKey: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<List<FileBatchResponse>> =
        ResponseEntity.ok(
            batchService.findByWindowAndDate(windowKey, date).map { FileBatchResponse.from(it) }
        )
}