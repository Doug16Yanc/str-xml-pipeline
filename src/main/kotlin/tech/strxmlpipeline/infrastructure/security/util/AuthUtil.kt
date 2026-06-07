package tech.strxmlpipeline.infrastructure.security.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.valueobject.OperatorName

@Component
class AuthUtil {

    /**
     * Extracts the authenticated user from the SecurityContext.
     * Returns [SystemUser] when the context is empty — covers scheduler
     * and Kafka consumer threads where no HTTP principal exists.
     */
    fun currentUser(): AuthenticatedUser {
        val auth = SecurityContextHolder.getContext().authentication
            ?: return SystemUser

        // Unauthenticated or anonymous — should not reach here past the filter chain,
        // but guard defensively
        if (!auth.isAuthenticated || auth.principal == "anonymousUser") {
            return SystemUser
        }

        val name  = auth.name
        val roles = auth.authorities.map { it.authority }
        val ispb  = auth.details
            .takeIf { it is Map<*, *> }
            ?.let { (it as Map<*, *>)["ispb"] as? String }

        return HumanUser(
            name  = name,
            roles = roles,
            ispb  = ispb,
        )
    }

    /**
     * Validates that the authenticated operator's ISPB matches the given [ispb].
     * No-op for system threads and users without an ISPB claim (ADMIN, BACEN_AUDITOR).
     * Throws [UnauthorizedParticipantException] if the ISPB does not match.
     */
    fun requireIspbOwnership(ispb: Ispb) {
        val user = currentUser()
        if (user is HumanUser && user.ispb != null && user.ispb != ispb.value) {
            throw UnauthorizedParticipantException(
                "Operator [${user.name}] with ISPB [${user.ispb}] " +
                        "is not authorized to act on behalf of ISPB [${ispb.value}]"
            )
        }
    }

    /**
     * Extracts and validates the ISPB embedded in the operator's [OperatorName].
     * Used when the ISPB claim is absent from the token but inferrable from the username.
     * Falls back gracefully if the name does not follow the {ispb}_{role}_{seq} pattern.
     */
    fun currentIspbFromName(): String? {
        val user = currentUser()
        if (user !is HumanUser) return null
        return runCatching { OperatorName(user.name).ispb }.getOrNull()
    }

    fun isSystemThread(): Boolean = currentUser() is SystemUser
}

