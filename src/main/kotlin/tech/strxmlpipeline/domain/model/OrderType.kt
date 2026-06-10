package tech.strxmlpipeline.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID


sealed class OrderType {
    abstract val code: String

    data object Ted : OrderType() { override val code = "TED" }
    data object Doc : OrderType() { override val code = "DOC" }
    data object Pix : OrderType() { override val code = "PIX" }
    data object Str : OrderType() { override val code = "STR" }

    companion object {
        fun fromCode(code: String): OrderType = when (code.uppercase()) {
            "TED" -> Ted
            "DOC" -> Doc
            "PIX" -> Pix
            "STR" -> Str
            else  -> throw IllegalArgumentException("Unknown order type: $code")
        }
    }
}
