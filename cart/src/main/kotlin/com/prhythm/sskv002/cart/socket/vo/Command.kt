package com.prhythm.sskv002.cart.socket.vo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val value: String
)
