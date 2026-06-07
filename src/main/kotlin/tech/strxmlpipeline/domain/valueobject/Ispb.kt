package tech.strxmlpipeline.domain.valueobject

@JvmInline
value class Ispb(val value: String) {
    init {
        require(value.length == 8 && value.all { it.isDigit() }) {
            "ISPB must contain exactly 8 numeric digits: $value"
        }
    }

    override fun toString(): String = value
}