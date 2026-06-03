package tech.strxmlpipeline.domain.enum

enum class RejectionReason(val code: String, val description: String) {
    INSUFFICIENT_FUNDS("001", "Insufficient funds in the reserve account"),
    INACTIVE_PARTICIPANT("002", "Participant not cleared for settlement"),
    INVALID_XSD("003", "XML document does not comply with the current XSD schema"),
    WINDOW_CLOSED("004", "Settlement window closed"),
    DUPLICATION("005", "Duplicate file detected by checksum evaluation"),
    INTERNAL_STR_ERROR("999", "Internal STR error");

    companion object {
        fun fromCode(code: String): RejectionReason =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown rejection code: $code")
    }
}