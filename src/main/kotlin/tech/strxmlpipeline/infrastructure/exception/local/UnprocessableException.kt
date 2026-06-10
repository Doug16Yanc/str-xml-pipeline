package tech.strxmlpipeline.infrastructure.exception.local

import java.util.UUID

class WindowCutoffExceededException(windowKey: String) :
    RuntimeException("Cutoff exceeded for window [$windowKey] — batch assembly aborted")

class InvalidOrderTransitionException(current: String, next: String, orderId: UUID) :
    RuntimeException("Invalid order transition: $current → $next [id=$orderId]")

class InvalidBatchTransitionException(current: String, next: String, batchId: UUID) :
    RuntimeException("Invalid batch transition: $current → $next [id=$batchId]")

class SelfSettlementException(ispb: String) :
    RuntimeException(
        "Internal settlement between same ISPB [$ispb] " +
                "must not transit through STR — resolve via internal ledger"
    )

class EmptyBatchException :
    RuntimeException("FileBatch must contain at least one order")

class InvalidBatchOrderStatusException(batchId: UUID) :
    RuntimeException("All orders must be in BATCHED status when assembled into FileBatch [$batchId]")

