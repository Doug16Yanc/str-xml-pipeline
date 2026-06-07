package tech.strxmlpipeline.web.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import tech.strxmlpipeline.domain.`user-command`.RegisterUserCommand
import tech.strxmlpipeline.infrastructure.security.util.PASSWORD_MESSAGE
import tech.strxmlpipeline.infrastructure.security.util.PASSWORD_PATTERN
import java.util.UUID

data class RegisterUserRequest(
    @field:NotBlank(message = "Name must not be blank")
    @field:Pattern(
        regexp = """^\d{8}_[a-z]{2,10}_\d{2}$""",
        message = "Name must follow pattern {ispb}_{role_abbrev}_{seq} — e.g. 60746948_op_01",
    )
    val name: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 9, message = "Password must be at least 9 characters")
    @field:Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    val password: String,

    @field:NotBlank(message = "Role ID must not be blank")
    val roleId: String,
) {
    fun toCommand() = RegisterUserCommand(
        name = name,
        password = password,
        roleId = UUID.fromString(roleId),
    )
}
