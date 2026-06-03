package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.FileBatch

interface XmlGeneratorPort {
    fun generate(batch: FileBatch): ByteArray
}