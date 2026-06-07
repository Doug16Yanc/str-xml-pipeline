package tech.strxmlpipeline.web.controller.auth

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.strxmlpipeline.domain.port.`in`.RegisterUserUseCase
import tech.strxmlpipeline.web.dto.auth.RegisterUserRequest
import tech.strxmlpipeline.web.dto.auth.RegisterUserResponse

@RestController
@RequestMapping("/v1/users")
class RegisterUserController(
    private val registerUseCase: RegisterUserUseCase,
) {

    @PostMapping("/create")
    fun register(
        @Valid @RequestBody request: RegisterUserRequest,
    ): ResponseEntity<RegisterUserResponse> {
        val user = registerUseCase.register(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                RegisterUserResponse(
                    id   = user.id,
                    name = user.name,
                    role = user.role.roleType,
                )
            )
    }
}