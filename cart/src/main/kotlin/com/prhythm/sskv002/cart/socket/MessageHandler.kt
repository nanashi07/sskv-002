package com.prhythm.sskv002.cart.socket

import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.cart.service.DirectiveDispatcher
import com.prhythm.sskv002.cart.socket.exception.MessageException
import com.prhythm.sskv002.cart.socket.vo.BadMessage
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class MessageHandler(
    private val dispatcher: DirectiveDispatcher,
    private val objectMapper: ObjectMapper
) : WebSocketHandler {

    private val log = LoggerFactory.getLogger(MessageHandler::class.java)
    private val activeSessions = hashMapOf<String, WebSocketSession>()

    fun send(sessionId: String, message: Any): Mono<Void> {
        return mono { activeSessions.getOrDefault(sessionId, null) }
            .flatMap { session ->
                log.debug("send message: {}", message)
                mono { message }
                    .map { objectMapper.writeValueAsString(it) }
                    .map { session.textMessage(it) }
                    .flatMap { session.send(Mono.just(it)) }
            }
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        if (!activeSessions.containsKey(session.id)) {
            activeSessions.putIfAbsent(session.id, session)
        }

        return session.receive()
            .filter { it.type == WebSocketMessage.Type.TEXT }
            .doOnNext { message ->
                dispatcher.parse(message.payloadAsText)
                    .doOnNext { directive -> directive.session = session.id }
                    .flatMap { dispatcher.dispatch(it) }
                    .onErrorResume(MessageException::class.java) { t ->
                        log.error("handle message error", t)
                        send(session.id, BadMessage(t.key, t.message!!))
                    }
                    .onErrorResume { t ->
                        log.error("handle message error", t)
                        send(session.id, BadMessage(null, "Bad message"))
                    }
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe()
            }
            .then()
    }

}