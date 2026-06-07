package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.FileBatch

interface XmlGeneratorPort {
    /**
     * Generates and XSD-validates the XML from [batch].
     * Orders are sorted by endToEndId before serialization to guarantee
     * a deterministic byte sequence — same logical content always produces the same SHA-256.
     */

    fun generate(batch: FileBatch): ByteArray
}