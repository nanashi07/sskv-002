package com.prhythm.sskv002.cart.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.cart.service.DirectiveDispatcher
import com.prhythm.sskv002.cart.socket.exception.DirectiveException
import com.prhythm.sskv002.cart.socket.vo.BadDirective
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

@Component
class DirectiveHandler(
    private val dispatcher: DirectiveDispatcher,
    private val objectMapper: ObjectMapper
) : WebSocketHandler {

    private val log = LoggerFactory.getLogger(DirectiveHandler::class.java)
    private val activeSessions = hashMapOf<String, WebSocketSession>()

    fun send(sessionId: String, message: Any): Mono<Void> {
        return mono { activeSessions.getOrDefault(sessionId, null) }
            .flatMap { session ->
                log.debug("send message: {}", message)
                mono { message }
                    .map { objectMapper.writeValueAsString(it) }
                    .map { session.textMessage(it) }
                    .flatMap { session.send(it.toMono()) }
            }
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        if (!activeSessions.containsKey(session.id)) {
            activeSessions.putIfAbsent(session.id, session)
        }

        val defaultMessage = "We encountered error in our system, please contact support.";

        return session.receive()
            .filter { it.type == WebSocketMessage.Type.TEXT }
            .doOnNext { message ->
                dispatcher.parse(message.payloadAsText)
                    .doOnNext { directive -> directive.session = session.id }
                    .flatMap { dispatcher.dispatch(it) }
                    .onErrorResume(DirectiveException::class.java) {
                        log.error("handle directive error", it)
                        send(session.id, BadDirective(it.key, it.message ?: defaultMessage))
                    }
                    .onErrorResume {
                        log.error("handle message error", it)
                        send(session.id, BadDirective(null, defaultMessage))
                    }
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe()
            }
            .then()
    }

}