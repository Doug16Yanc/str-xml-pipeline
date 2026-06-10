package tech.strxmlpipeline.infrastructure.exception.local

import tech.strxmlpipeline.domain.enum.RoleType
import java.util.UUID

class RoleNotFoundException(identifier: Any) :
    RuntimeException("Role not found for identifier: $identifier")

class ParticipantNotFoundException(ispb: String) :
    RuntimeException("Participant not found for ISPB: $ispb")

class FileBatchNotFoundException(id: UUID) :
    RuntimeException("FileBatch not found: $id")

class SettlementOrderNotFoundException(id: UUID) :
    RuntimeException("Settlement order not found: $id")

class XmlFileNotFoundException(detail: String) :
    RuntimeException("XmlFile not found — $detail")

class SettlementReturnNotFoundException(batchId: UUID) :
    RuntimeException("Settlement return not found for batch: $batchId")
