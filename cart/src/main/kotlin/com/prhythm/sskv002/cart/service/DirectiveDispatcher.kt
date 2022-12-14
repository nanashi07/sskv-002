package com.prhythm.sskv002.cart.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.cart.socket.exception.DirectiveException
import com.prhythm.sskv002.cart.socket.vo.BookingDirective
import com.prhythm.sskv002.cart.socket.vo.Directive
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DirectiveDispatcher(
    private val mapper: ObjectMapper,
    private val cartService: CartService,
) {

    private val log = LoggerFactory.getLogger(DirectiveDispatcher::class.java)

    fun parse(message: String): Mono<Directive> {
        log.debug("parse message: $message")
        return mono { mapper.readValue(message, Directive::class.java) }
            .doOnNext { validateCommand(it) }
    }

    private fun validateCommand(directive: Directive): Boolean {
        return when (directive.command) {
            "booking" -> true
            else -> throw DirectiveException("Invalid command: ${directive.command}")
                .apply { key = directive.key }
        }
    }

    fun dispatch(directive: Directive): Mono<Void> {
        log.info("dispatch: {}", directive)

        // DUMMY: handle commands dispatch
        return mono { BookingDirective(directive) }
            .flatMap { cartService.validate(it) }
            .flatMap { cartService.booking(it) }
    }

}