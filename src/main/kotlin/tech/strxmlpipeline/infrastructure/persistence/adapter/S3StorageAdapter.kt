package tech.strxmlpipeline.infrastructure.persistence.adapter

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import tech.strxmlpipeline.domain.port.out.XmlFileStoragePort
import tech.strxmlpipeline.domain.valueobject.S3Key
import tech.strxmlpipeline.infrastructure.exception.local.S3DownloadException
import tech.strxmlpipeline.infrastructure.exception.local.S3UploadException

@Component
class S3StorageAdapter(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket}") private val bucket: String
) : XmlFileStoragePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun upload(content: ByteArray, s3Key: S3Key): S3Key {
        try {
            val request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key.value)
                .contentType("application/xml")
                .contentLength(content.size.toLong())
                .build()

            s3Client.putObject(request, RequestBody.fromBytes(content))

            log.info("Uploaded XML to S3 — key [{}] — {} bytes", s3Key.value, content.size)
            return s3Key
        } catch (e: S3Exception) {
            throw S3UploadException("Failed to upload XML to S3 [key=${s3Key.value}]: ${e.message}", e)
        }
    }

    override fun download(s3Key: S3Key): ByteArray {
        try {
            val request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key.value)
                .build()

            return s3Client.getObject(request).readAllBytes()
        } catch (e: S3Exception) {
            throw S3DownloadException("Failed to download XML from S3 [key=${s3Key.value}]: ${e.message}", e)
        }
    }
}