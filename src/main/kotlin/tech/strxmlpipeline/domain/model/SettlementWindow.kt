package tech.strxmlpipeline.domain.model

import java.time.LocalTime
import java.time.Clock

/**
 * Value object representing a settlement window.
 * Canonical format: "{SYSTEM}-{CYCLE}-{TIME}" — e.g., "STR-D1-07h30".
 * This serves as the Kafka partitioning key: all orders in the same window
 * routes to the same partition, ensuring temporal order without blocking database locks.
 */
data class SettlementWindow(
    val system: String,
    val cycle: String,
    val time: LocalTime
) {
    val partitioningKey: String
        get() = "$system-$cycle-${time.hour.toString().padStart(2, '0')}h${time.minute.toString().padStart(2, '0')}"

    /**
     * Validates whether the current instant is still within the window cutoff.
     * Receives [clock] externally so the domain remains testable without mocking static time.
     * The scheduler fires 5 minutes before cutoff — if this returns false, orders go to REJECTED_CUTOFF.
     */
    fun isOpen(clock: Clock): Boolean =
        LocalTime.now(clock).isBefore(time)

    override fun toString(): String = partitioningKey

    companion object {
        private val FORMAT = Regex("""^([A-Z]+)-([A-Z0-9]+)-(\d{2})h(\d{2})$""")

        fun current(clock: Clock = Clock.systemDefaultZone()): SettlementWindow {
            val now = LocalTime.now(clock)
            return SettlementWindow(
                system = "STR",
                cycle  = "D1",
                time   = LocalTime.of(now.hour, now.minute),
            )
        }

        fun parse(value: String): SettlementWindow {
            val match = FORMAT.matchEntire(value)
                ?: throw IllegalArgumentException("Invalid window format: '$value'. Expected: SYSTEM-CYCLE-HHhMM")

            val (system, cycle, hour, minute) = match.destructured

            return SettlementWindow(
                system = system,
                cycle = cycle,
                time = LocalTime.of(hour.toInt(), minute.toInt()),
            )
        }
    }
}