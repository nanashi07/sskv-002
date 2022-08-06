package com.prhythm.sskv002.cart.socket.vo

data class Directive(
    var session: String?,
    var command: String,
    var key: String,
    var data: Any?
)