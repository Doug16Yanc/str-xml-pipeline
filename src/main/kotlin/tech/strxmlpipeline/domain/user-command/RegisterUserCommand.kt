package tech.strxmlpipeline.domain.`user-command`

import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.domain.valueobject.OperatorName
import java.util.UUID

data class RegisterUserCommand(
    val name: String,
    val password: String,
    val roleId: UUID
) {
    fun extractIspb(): Ispb = Ispb(OperatorName(name).ispb)
}
