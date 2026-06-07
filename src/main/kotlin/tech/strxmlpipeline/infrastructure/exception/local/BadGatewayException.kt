package tech.strxmlpipeline.infrastructure.exception.local

import java.util.UUID

class S3UploadException(s3Key: String, cause: Throwable? = null) :
    RuntimeException("Failed to upload XML to S3 [key=$s3Key]", cause)

class S3DownloadException(s3Key: String, cause: Throwable? = null) :
    RuntimeException("Failed to download XML from S3 [key=$s3Key]", cause)

class XmlGenerationException(batchId: UUID, cause: Throwable? = null) :
    RuntimeException("Failed to generate XML for batch [$batchId]", cause)

class StrReturnParseException(detail: String, cause: Throwable? = null) :
    RuntimeException("Failed to parse STR return XML — $detail", cause)
