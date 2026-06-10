package tech.strxmlpipeline.infrastructure.exception.local

import java.util.UUID

class UserAlreadyExistsException(name: String) :
    RuntimeException("User already registered: $name")

class ParticipantAlreadyExistsException(ispb: String) :
    RuntimeException("Participant already registered for ISPB: $ispb")

class DuplicateActiveBatchException(windowKey: String, date: String) :
    RuntimeException("Active batch already exists for window [$windowKey] on [$date]")

class DuplicateSettlementReturnException(batchId: UUID) :
    RuntimeException("Return for batch [$batchId] was already recorded — idempotent discard") {
    val batchId: UUID = batchId
}



