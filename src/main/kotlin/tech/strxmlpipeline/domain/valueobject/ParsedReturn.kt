package tech.strxmlpipeline.domain.valueobject

data class ParsedReturn(
    val batchId: String,
    val result: String,
    val messageCode: String,
    val rejectionCode: String?,
    val description: String?
)