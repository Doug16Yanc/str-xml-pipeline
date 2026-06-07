package tech.strxmlpipeline.infrastructure.security.util

data class HumanUser(
    val name: String,
    val roles: List<String?>,
    val ispb: String?,
) : AuthenticatedUser() {
    fun hasRole(role: String): Boolean = "ROLE_$role" in roles || role in roles
    fun isAdmin(): Boolean = hasRole("ADMIN")
    fun isAuditor(): Boolean = hasRole("BACEN_AUDITOR")
    fun isOperator(): Boolean = hasRole("SETTLEMENT_OPERATOR")
}
