package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.valueobject.S3Key

interface XmlFileStoragePort {
    fun upload(fileContent: ByteArray, s3Key: S3Key): S3Key
    fun download(s3Key: S3Key): ByteArray
}