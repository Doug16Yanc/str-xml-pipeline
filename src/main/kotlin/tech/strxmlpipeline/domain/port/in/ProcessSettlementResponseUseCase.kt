package tech.strxmlpipeline.domain.port.`in`

import tech.strxmlpipeline.domain.model.SettlementResponse

interface ProcessSettlementResponseUseCase {
    /**
     * Entry point for the BACEN/STR clearing response consumer.
     * Receives the raw response payload and executes the status transitions.
     */
    fun process(response: SettlementResponse)
}