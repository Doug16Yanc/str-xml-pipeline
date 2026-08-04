package tech.strxmlpipeline.domain.enum

/**
 * Sealed state machine for [tech.strxmlpipeline.domain.model.FileBatch], extended
 * for the Terceira Milha saga (choreographed compensation — see README diagram).
 *
 * PENDING -> EMITTED -> TRANSMITTED -> ACCEPTED (terminal)
 *                                    \-> TRANSMISSION_REJECTED -> COMPENSATING -> COMPENSATED (terminal)
 *
 * There is no orchestrator: the versioned status itself (`version`, `updated_at`
 * on FileBatchEntity) is the saga's coordination/audit trail. Each consumer reacts
 * to the status it cares about and moves the batch forward.
 */
enum class BatchStatus {
    /** Batch assembled from BATCHED orders, not yet emitted as XML. */
    PENDING,

    /** XML generated, checksummed and uploaded to S3 (Segunda Milha). */
    EMITTED,

    /** XML successfully sent to the BACEN/STR API — awaiting async settlement return. */
    TRANSMITTED,

    /** BACEN/STR accepted the batch. Terminal — no further transitions. */
    ACCEPTED,

    /** BACEN/STR rejected the batch. Triggers the compensation saga. */
    TRANSMISSION_REJECTED,

    /** Compensating consumer is reverting orders and freeing them for a new window. */
    COMPENSATING,

    /** Compensation complete — orders released, batch closed. Terminal. */
    COMPENSATED;

    val isTerminal: Boolean
        get() = this == ACCEPTED || this == COMPENSATED

    fun canTransitionTo(destination: BatchStatus): Boolean = when (this) {
        PENDING                -> destination == EMITTED
        EMITTED                -> destination == TRANSMITTED
        TRANSMITTED            -> destination == ACCEPTED || destination == TRANSMISSION_REJECTED
        ACCEPTED                -> false
        TRANSMISSION_REJECTED  -> destination == COMPENSATING
        COMPENSATING           -> destination == COMPENSATED
        COMPENSATED             -> false
    }
}