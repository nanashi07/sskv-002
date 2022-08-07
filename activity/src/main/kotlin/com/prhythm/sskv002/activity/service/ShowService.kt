package com.prhythm.sskv002.activity.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.activity.service.vo.BookOrder
import com.prhythm.sskv002.activity.service.vo.BookShow
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ShowService(
    private val mapper: ObjectMapper,
    private val messageManageService: MessageManageService,
) {

    fun createBooking(message: String, partitionKey: String): Mono<Void> {
        return parseBookMessage(message)
            .flatMapMany { split(it, partitionKey) }
            .flatMap { messageManageService.sendCoordinate(it) }
            .then()
    }

    private fun parseBookMessage(message: String): Mono<BookOrder> {
        return mono { mapper.readValue(message, BookOrder::class.java) }
    }

    private fun split(bookOrder: BookOrder, source: String): Flux<BookShow> {
        return Flux.fromIterable(bookOrder.items)
            .map { BookShow(bookOrder.session, bookOrder.key, source, it.eventId, it.showId, it.quantity) }
    }

}