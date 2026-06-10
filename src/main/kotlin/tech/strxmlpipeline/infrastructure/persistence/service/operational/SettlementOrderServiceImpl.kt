package tech.strxmlpipeline.infrastructure.persistence.service.operational

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.ParticipantPort
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.infrastructure.exception.local.ParticipantNotFoundException
import tech.strxmlpipeline.infrastructure.exception.local.SettlementOrderNotFoundException
import tech.strxmlpipeline.infrastructure.security.util.AuthUtil
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SettlementOrderServiceImpl(
    private val orderPort: SettlementOrderPort,
    private val participantPort: ParticipantPort,
    private val authUtil: AuthUtil,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun findById(id: UUID): SettlementOrder {
        val order = orderPort.findById(id) ?: throw SettlementOrderNotFoundException(id)
        authUtil.validateReadAccess(order)
        return order
    }

    fun findByBatchId(batchId: UUID): List<SettlementOrder> {
        val orders = orderPort.findByBatchId(batchId)
        orders.forEach { authUtil.validateReadAccess(it) }
        return orders
    }

    fun findByStatus(status: OrderStatus): List<SettlementOrder> {
        val orders = orderPort.findByStatus(status)
        return authUtil.filterOrdersForCurrentTenant(orders)
    }

    @Transactional
    fun create(order: SettlementOrder, window: SettlementWindow): SettlementOrder {
        authUtil.validateMutationAccess(order.originator.ispb)

        participantPort.findByIspb(order.originator.ispb)
            ?: throw ParticipantNotFoundException("Originator not found — ISPB: ${order.originator.ispb}")

        participantPort.findByIspb(order.destination.ispb)
            ?: throw ParticipantNotFoundException("Destination not found — ISPB: ${order.destination.ispb}")

        return orderPort.save(order, window).also {
            log.info("Settlement order created — id [{}] type [{}] amount [{}]", it.id, it.type.code, it.amount)
        }
    }
}