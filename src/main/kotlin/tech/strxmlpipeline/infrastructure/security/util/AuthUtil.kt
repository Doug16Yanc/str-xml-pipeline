package tech.strxmlpipeline.infrastructure.security.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.valueobject.Ispb
import tech.strxmlpipeline.domain.valueobject.OperatorName
import tech.strxmlpipeline.infrastructure.exception.local.UnauthorizedParticipantException

@Component
class AuthUtil {

    fun currentUser(): AuthenticatedUser {
        val auth = SecurityContextHolder.getContext().authentication ?: return SystemUser

        if (!auth.isAuthenticated || auth.principal == "anonymousUser") {
            return SystemUser
        }

        val name  = auth.name
        val roles = auth.authorities.map { it.authority }.filterNotNull()
        val ispb  = auth.details
            .takeIf { it is Map<*, *> }
            ?.let { (it as Map<*, *>)["ispb"] as? String }

        return HumanUser(
            name  = name,
            roles = roles,
            ispb  = ispb,
        )
    }

    fun validateMutationAccess(originatorIspb: Ispb) {
        when (val user = currentUser()) {
            is HumanUser -> {
                if (user.domainIspb != null && user.domainIspb != originatorIspb) {
                    throw UnauthorizedParticipantException(
                        operatorIspb = user.ispb ?: "UNKNOWN",
                        originatorIspb = originatorIspb.value
                    )
                }
            }
            is SystemUser -> {}
        }
    }

    fun validateReadAccess(order: SettlementOrder) {
        when (val user = currentUser()) {
            is HumanUser -> {
                if (user.domainIspb != null &&
                    user.domainIspb != order.originator.ispb &&
                    user.domainIspb != order.destination.ispb) {

                    throw UnauthorizedParticipantException(
                        "Access denied. Operator ISPB [${user.ispb}] cannot access order [${order.id}]"
                    )
                }
            }
            is SystemUser -> {}
        }
    }

    fun filterOrdersForCurrentTenant(orders: List<SettlementOrder>): List<SettlementOrder> {
        return when (val user = currentUser()) {
            is HumanUser -> {
                if (user.domainIspb != null) {
                    orders.filter { it.originator.ispb == user.domainIspb || it.destination.ispb == user.domainIspb }
                } else {
                    orders
                }
            }
            is SystemUser -> orders
        }
    }

    fun currentIspbFromName(): String? {
        val user = currentUser()
        if (user !is HumanUser) return null
        if (user.ispb != null) return user.ispb
        return runCatching { OperatorName(user.name).ispb }.getOrNull()
    }

    fun isSystemThread(): Boolean = currentUser() is SystemUser
}