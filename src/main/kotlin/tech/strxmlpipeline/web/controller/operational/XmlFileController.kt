package tech.strxmlpipeline.web.controller.operational

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.domain.port.out.XmlFileStoragePort
import tech.strxmlpipeline.infrastructure.persistence.service.operational.XmlFileServiceImpl
import tech.strxmlpipeline.web.dto.response.XmlFileResponse

import java.util.UUID

@RestController
@RequestMapping("/v1/files")
class XmlFileController(
    private val xmlFileService: XmlFileServiceImpl,
    private val storagePort: XmlFileStoragePort
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<XmlFileResponse> =
        ResponseEntity.ok(
            XmlFileResponse.from(xmlFileService.findById(id))
        )

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_OPERATOR', 'BACEN_AUDITOR', 'ADMIN')")
    fun findByBatchId(@PathVariable batchId: UUID): ResponseEntity<XmlFileResponse> =
        ResponseEntity.ok(
            XmlFileResponse.from(xmlFileService.findByBatchId(batchId))
        )

    @GetMapping("/checksum/{checksum}")
    @PreAuthorize("hasAnyAuthority('BACEN_AUDITOR', 'ADMIN')")
    fun findByChecksum(@PathVariable checksum: String): ResponseEntity<XmlFileResponse> =
        ResponseEntity.ok(
            XmlFileResponse.from(xmlFileService.findByChecksum(checksum))
        )

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('BACEN_AUDITOR', 'ADMIN')")
    fun download(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val file    = xmlFileService.findById(id)
        val content = storagePort.download(file.s3Key)
        val headers = HttpHeaders().apply {
            contentType        = MediaType.APPLICATION_XML
            contentLength      = content.size.toLong()
            contentDisposition = ContentDisposition
                .attachment()
                .filename("str-${file.batchId}-${file.id}.xml")
                .build()
            set("X-Checksum-SHA256", file.checksumSha256)
            set("X-XSD-Version", file.xsdVersion)
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(content)
    }
}