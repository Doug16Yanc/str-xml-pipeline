package tech.strxmlpipeline.infrastructure.persistence.adapter

import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.model.SettlementOrder
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.util.UUID

@Component
class SettlementOrderCopyBulkAdapter(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun bulkInsertUsingCopy(orders: List<SettlementOrder>, batchId: UUID) {
        if (orders.isEmpty()) return

        jdbcTemplate.execute { connection: Connection ->
            val baseConnection: BaseConnection = connection.unwrap(BaseConnection::class.java)

            val copyManager = CopyManager(baseConnection)

            val copySql = """
                COPY settlement_order (
                    id, originator_id, destination_id, batch_id, order_type, 
                    amount, currency, settlement_date, status, created_at
                ) FROM STDIN WITH (FORMAT csv, DELIMITER ',', FORCE_NULL(batch_id))
            """.trimIndent()

            val csvBuilder = StringBuilder()

            orders.forEach { order ->
                csvBuilder.append(order.id).append(",")
                csvBuilder.append(order.originator.ispb.value).append(",")
                csvBuilder.append(order.destination.ispb.value).append(",")
                csvBuilder.append(batchId).append(",")
                csvBuilder.append(order.type.code).append(",")
                csvBuilder.append(order.amount).append(",")
                csvBuilder.append("BRL").append(",")
                csvBuilder.append(order.settlementDate).append(",")
                csvBuilder.append("BATCHED").append(",")
                csvBuilder.append(order.createdAt).append("\n")
            }

            val bytes = csvBuilder.toString().toByteArray(StandardCharsets.UTF_8)

            ByteArrayInputStream(bytes).use { inputStream ->
                copyManager.copyIn(copySql, inputStream, 65536)
            }
        }
    }
}