package tech.strxmlpipeline.infrastructure.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.`in`.AssembleFileBatchUseCase

/**
 * Pull-based scheduler that triggers batch assembly for each configured settlement window.
 *
 * Each method fires slightly before its window cutoff to give the system
 * enough time to fetch orders, run invariant checks, and publish to Kafka
 * before the STR deadline.
 *
 * Cron expressions run in system default timezone — configure
 * `spring.task.scheduling.pool.size` and `spring.jackson.time-zone` accordingly.
 *
 * Windows defined here must mirror the windows registered in the database and
 * the Kafka topic partition layout. Adding a new window = new @Scheduled method
 * + new partition key in the topic config.
 */
@Component
class SettlementWindowScheduler(
    private val assembleUseCase: AssembleFileBatchUseCase,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // STR D1 — first window of the day, 07h30 cutoff
    // Fires at 07h25 — 5 min buffer for order fetch + XML pre-validation
    @Scheduled(cron = "0 55 07 * * MON-FRI", zone = "America/Sao_Paulo")
    fun strD1Window0730() = trigger("STR-D1-17h00")

    // STR D1 — second window, 10h00 cutoff
    @Scheduled(cron = "0 55 9 * * MON-FRI", zone = "America/Sao_Paulo")
    fun strD1Window1000() = trigger("STR-D1-10h00")

    // STR D1 — third window, 14h00 cutoff
    @Scheduled(cron = "0 55 13 * * MON-FRI", zone = "America/Sao_Paulo")
    fun strD1Window1400() = trigger("STR-D1-14h00")

    // STR D0 — same-day settlement, 17h00 cutoff
    @Scheduled(cron = "0 55 16 * * MON-FRI", zone = "America/Sao_Paulo")
    fun strD0Window1700() = trigger("STR-D0-17h00")

    private fun trigger(windowKey: String) {
        log.info("Scheduler triggering assembly for window [{}]", windowKey)
        runCatching { assembleUseCase.assemble(windowKey) }
            .onFailure { e ->
                // Log and swallow — a scheduler exception must not crash the Spring context.
                // Ops must be alerted via the log/metrics pipeline.
                log.error("Assembly failed for window [{}]: {}", windowKey, e.message, e)
            }
    }
}