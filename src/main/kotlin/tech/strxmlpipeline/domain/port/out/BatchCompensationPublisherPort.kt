package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.model.FileBatch

interface BatchCompensationPublisherPort {
    /**
     * Publishes the event that kicks off the choreographed compensation saga
     * for a batch that just entered TRANSMISSION_REJECTED. Must only be called
     * after the DB transaction that persisted that status has committed.
     */
    fun publishCompensation(batch: FileBatch, rejectionReason: RejectionReason?)
}