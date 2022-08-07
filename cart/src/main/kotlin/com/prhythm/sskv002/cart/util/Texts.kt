package com.prhythm.sskv002.cart.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.Serializable

class Texts(private val value: Any?, private val format: Format) {
    override fun toString(): String {
        return when (format) {
            Format.Json -> value.run { mapper.writeValueAsString(this) }
            Format.Text -> value.run { toString() }
        }
    }
}

enum class Format {
    Text,
    Json,
    ;
}

private val mapper = ObjectMapper()

fun <T> T.logJson(): Texts where T : Serializable {
    return Texts(this, Format.Json)
}