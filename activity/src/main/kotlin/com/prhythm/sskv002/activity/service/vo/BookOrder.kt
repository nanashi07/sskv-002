package com.prhythm.sskv002.activity.service.vo

data class BookOrder(
    val session: String,
    val key: String,
    val items: List<BookItem>
)