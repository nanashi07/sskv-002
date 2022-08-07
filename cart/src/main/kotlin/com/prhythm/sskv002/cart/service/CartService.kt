package com.prhythm.sskv002.cart.service

import com.prhythm.sskv002.cart.socket.exception.DirectiveException
import com.prhythm.sskv002.cart.socket.vo.BookingDirective
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class CartService(
    private val messageService: MessageService,
) {

    fun validate(directive: BookingDirective): Mono<BookingDirective> {
        // DUMMY: validate booking quantity
        // DUMMY: validate booking amount
        // DUMMY: check duplicated booking
        // DUMMY: check ongoing booking request
        // DUMMY: check customer credit record
        return mono { directive }
    }

    fun booking(directive: BookingDirective): Mono<Void> {
        return directive.toMono()
            .flatMap { messageService.sendBookingOrder(it) }
            .onErrorResume {
                // notify booking error
                Mono.error(DirectiveException("Unable to book your request, please try again later.", it)
                    .apply { key = directive.key })
            }
    }

}