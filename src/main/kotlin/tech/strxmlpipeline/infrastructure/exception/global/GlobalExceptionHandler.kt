package tech.strxmlpipeline.infrastructure.exception.global

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tech.strxmlpipeline.infrastructure.exception.local.DuplicateActiveBatchException
import tech.strxmlpipeline.infrastructure.exception.local.DuplicateSettlementReturnException
import tech.strxmlpipeline.infrastructure.exception.local.FileBatchNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.InvalidCredentialsException
import tech.strxmlpipeline.infrastructure.exception.local.InvalidTokenException
import tech.strxmlpipeline.infrastructure.exception.local.ParticipantAlreadyExistsException
import tech.strxmlpipeline.infrastructure.exception.local.ParticipantNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.RoleNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.S3DownloadException
import tech.strxmlpipeline.infrastructure.exception.local.S3UploadException
import tech.strxmlpipeline.infrastructure.exception.local.SettlementOrderNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.SettlementReturnNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.StrReturnParseException
import tech.strxmlpipeline.infrastructure.exception.local.TokenExpiredException
import tech.strxmlpipeline.infrastructure.exception.local.UnauthorizedParticipantException
import tech.strxmlpipeline.infrastructure.exception.local.UserAlreadyExistsException
import tech.strxmlpipeline.infrastructure.exception.local.WindowCutoffExceededException
import tech.strxmlpipeline.infrastructure.exception.local.XmlFileNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.XmlGenerationException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(
        RoleNotFoundException::class,
        ParticipantNotFoundException::class,
        FileBatchNotFoundException::class,
        SettlementOrderNotFoundException::class,
        XmlFileNotFoundException::class,
        SettlementReturnNotFoundException::class,
    )
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(
        UserAlreadyExistsException::class,
        ParticipantAlreadyExistsException::class,
        DuplicateActiveBatchException::class,
        DuplicateSettlementReturnException::class,
    )
    fun handleConflict(ex: RuntimeException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.CONFLICT, ex.message)

    @ExceptionHandler(UnauthorizedParticipantException::class)
    fun handleUnauthorized(ex: UnauthorizedParticipantException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.FORBIDDEN, ex.message)

    @ExceptionHandler(
        InvalidCredentialsException::class,
        InvalidTokenException::class
    )
    fun handleInvalidViolation(ex: RuntimeException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.BAD_REQUEST, ex.message)

    @ExceptionHandler(
        IllegalArgumentException::class,
        IllegalStateException::class,
        WindowCutoffExceededException::class,
    )
    fun handleDomainViolation(ex: RuntimeException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.UNPROCESSABLE_ENTITY, ex.message)

    @ExceptionHandler(
        TokenExpiredException::class
    )
    fun handleTokenExpired(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return error(HttpStatus.FORBIDDEN, ex.message)
    }

    @ExceptionHandler(
        S3UploadException::class,
        S3DownloadException::class,
        XmlGenerationException::class,
        StrReturnParseException::class,
    )
    fun handleExternalFailure(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error("External system failure: {}", ex.message, ex)
        return error(HttpStatus.BAD_GATEWAY, "External system failure — check logs for details")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult?.fieldErrors
            ?.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return error(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error: {}", ex.message, ex)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
    }


    private fun error(status: HttpStatus, message: String?): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status  = status.value(),
                    error   = status.reasonPhrase,
                    message = message ?: "No message available",
                )
            )
}
