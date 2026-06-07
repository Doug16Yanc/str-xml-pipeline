package tech.strxmlpipeline.infrastructure.exception.global

import java.time.OffsetDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
