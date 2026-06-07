package tech.strxmlpipeline.domain.port.`in`

interface LogoutUseCase {
    fun execute(accessToken: String)
}