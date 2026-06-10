package tech.strxmlpipeline.web.controller.operational

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.domain.enum.ParticipantType
import tech.strxmlpipeline.infrastructure.persistence.service.operational.ParticipantServiceImpl
import tech.strxmlpipeline.web.dto.request.CreateParticipantRequest
import tech.strxmlpipeline.web.dto.response.ParticipantResponse

@RestController
@RequestMapping("/v1/participants")
class ParticipantController(
    private val participantService: ParticipantServiceImpl,
) {

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findAll(): ResponseEntity<List<ParticipantResponse>> =
        ResponseEntity.ok(
            participantService.findAll().map { ParticipantResponse.from(it) }
        )

    @GetMapping("/{ispb}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByIspb(@PathVariable ispb: String): ResponseEntity<ParticipantResponse> =
        ResponseEntity.ok(
            ParticipantResponse.from(participantService.findByIspb(ispb))
        )

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun create(@RequestBody request: CreateParticipantRequest): ResponseEntity<ParticipantResponse> {
        val participantDomain = request.toDomain(type = ParticipantType.MULTIPLE_BANK)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ParticipantResponse.from(participantService.create(participantDomain)))
    }

    @PutMapping("/update/{ispb}")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun update(
        @PathVariable ispb: String,
        @RequestBody request: CreateParticipantRequest,
    ): ResponseEntity<ParticipantResponse> {
        require(ispb == request.ispb) {
            "ISPB in path [$ispb] does not match ISPB in body [${request.ispb}]"
        }

        val participantDomain = request.toDomain(type = ParticipantType.MULTIPLE_BANK)

        return ResponseEntity.ok(
            ParticipantResponse.from(participantService.update(participantDomain))
        )
    }
}