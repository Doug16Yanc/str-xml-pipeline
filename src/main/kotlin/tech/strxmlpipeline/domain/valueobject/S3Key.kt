package tech.strxmlpipeline.domain.valueobject

@JvmInline
value class S3Key(val value: String) {
    init {
        require(value.isNotBlank()) { "S3Key cannot be empty" }
    }

    override fun toString(): String = value
}