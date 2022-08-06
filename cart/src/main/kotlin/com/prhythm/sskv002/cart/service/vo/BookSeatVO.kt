package com.prhythm.sskv002.cart.service.vo

data class BookSeatVO(
    val activityId: String,
    val showId: String,
    val groupId: String?,
    val quantity: Int,
    val consecutive: Boolean,
)
