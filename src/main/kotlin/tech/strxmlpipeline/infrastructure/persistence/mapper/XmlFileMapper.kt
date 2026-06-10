package tech.strxmlpipeline.infrastructure.persistence.mapper

import tech.strxmlpipeline.domain.model.XmlFile
import tech.strxmlpipeline.domain.valueobject.S3Key
import tech.strxmlpipeline.infrastructure.persistence.entity.XmlFileEntity

import java.time.ZoneOffset

fun XmlFileEntity.toXmlFileDomain(): XmlFile = XmlFile(
    id = this.id,
    batchId = this.batchId,
    s3Key = S3Key(this.s3Key),
    checksumSha256 = this.checksumSha256,
    xsdVersion = this.xsdVersion,
    sizeBytes = this.sizeBytes,
    generatedAt = this.emittedAt
)

fun XmlFile.toXmlFileEntity(): XmlFileEntity = XmlFileEntity(
    id = this.id,
    batchId = this.batchId,
    s3Bucket = "str-xml-storage-bucket",
    s3Key = this.s3Key.value,
    checksumSha256 = this.checksumSha256,
    sizeBytes = this.sizeBytes,
    xsdVersion = this.xsdVersion,
    emittedAt = this.generatedAt
)