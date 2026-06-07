package tech.strxmlpipeline.domain.valueobject

@JvmInline
value class OperatorName(val value: String) {
    init {
        require(value.matches(Regex("""^\d{8}_[a-z]{2,10}_\d{2}$"""))) {
            "OperatorName must follow pattern {ispb}_{role_abbrev}_{seq} — e.g. 60746948_op_01"
        }
    }

    val ispb: String get() = value.substringBefore("_")
    val roleAbbrev: String get() = value.split("_")[1]
    val seq: String get() = value.substringAfterLast("_")

    override fun toString(): String = value
}