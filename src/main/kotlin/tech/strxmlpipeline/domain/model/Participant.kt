package tech.strxmlpipeline.domain.model

data class Participant(
    val ispb: Ispb,
    val name: String,
    val account: String?,
    val branch: String?
)

@JvmInline
value class Ispb(val value: String) {
    init {
        require(value.length == 8 && value.all { it.isDigit() }) {
            "ISPB must contain exactly 8 numeric digits: $value"
        }
    }

    override fun toString(): String = value
}