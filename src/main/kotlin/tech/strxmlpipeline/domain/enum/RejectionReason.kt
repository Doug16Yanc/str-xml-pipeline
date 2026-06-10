package tech.strxmlpipeline.domain.enum

enum class RejectionReason(val code: String, val description: String) {
    INSUFFICIENT_RESERVES("001", "Insufficient balance in reserve account"),
    INACTIVE_PARTICIPANT("002", "Participant not enabled for settlement"),
    INVALID_XSD("003", "XML document does not conform to active XSD"),
    WINDOW_CLOSED("004", "Settlement window already closed"),
    DUPLICATE_FILE("005", "Duplicate file detected by checksum"),
    STR_INTERNAL_ERROR("999", "Internal STR error");

    companion object {
        fun fromCode(code: String): RejectionReason =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown rejection code: $code")
    }
}

