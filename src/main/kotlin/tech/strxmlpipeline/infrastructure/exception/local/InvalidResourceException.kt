package tech.strxmlpipeline.infrastructure.exception.local

class InvalidCredentialsException :
    RuntimeException("Invalid name or password")

class InvalidTokenException(detail: String = "Invalid or expired token") :
    RuntimeException(detail)