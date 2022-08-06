package com.prhythm.sskv002.cart.constant

enum class OrderStatus(val code: Int) {

    CREATED(10),

    CANCELED(40),
    SUCCESS(50),
    PARTIAL_SUCCESS(55),
    FAILED(60),

    UNKNOWN(99),
    ;

}