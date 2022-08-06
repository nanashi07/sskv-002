package com.prhythm.sskv002.cart.service

import com.prhythm.sskv002.cart.service.vo.BookSeatVO
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CartService {

    fun book(cartId: String, items: List<BookSeatVO>): Mono<Void> {
        // TODO: send message by seat request
        return Mono.just("").then()
    }

}