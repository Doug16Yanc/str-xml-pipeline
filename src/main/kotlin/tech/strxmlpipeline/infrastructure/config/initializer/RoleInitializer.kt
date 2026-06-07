package tech.strxmlpipeline.infrastructure.config.initializer

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.RoleType
import tech.strxmlpipeline.domain.port.`in`.ManageRoleUseCase
import tech.strxmlpipeline.infrastructure.exception.local.RoleNotFoundException

@Component
class RoleInitializer(
    private val manageRoleUseCase: ManageRoleUseCase
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(RoleInitializer::class.java)

    override fun run(vararg args: String?) {
        val rolesToInitialize = listOf(
            RoleType.ADMIN,
            RoleType.BACEN_AUDITOR,
            RoleType.SETTLEMENT_OPERATOR
        )

        for (roleType in rolesToInitialize) {
            try {
                manageRoleUseCase.findByType(roleType)
                logger.info("Role already exists: ${roleType.name}")
            } catch (e: RoleNotFoundException) {
                logger.info("Role not found. Creating role: ${roleType.name}")
                manageRoleUseCase.createRole(roleType)
            } catch (e: Exception) {
                logger.error("Error checking/creating role ${roleType.name}: ${e.message}", e)
            }
        }
    }
}