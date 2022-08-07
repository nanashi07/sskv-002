package com.prhythm.sskv002.activity.service.vo

class BookShow(
    val session: String,
    val key: String,
    val source: String,
    val eventId: String,
    val showId: String,
    val quantity: Int = 0
)