package com.prhythm.sskv002.cart.socket.vo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.cart.service.vo.CartItem
import java.io.Serializable

@Command("booking")
class BookingDirective(directive: Directive) : Serializable {

    val session: String
    val key: String
    val items: List<CartItem>

    init {
        this.session = directive.session
        this.key = directive.key
        this.items = mapper.convertValue(directive.data, object : TypeReference<List<CartItem>>() {})
    }
}

private val mapper = ObjectMapper()
