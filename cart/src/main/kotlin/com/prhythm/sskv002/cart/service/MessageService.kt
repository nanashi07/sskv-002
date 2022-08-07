package com.prhythm.sskv002.cart.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.prhythm.sskv002.cart.constant.MessageQueue
import com.prhythm.sskv002.cart.message.KafkaGroupAdvisor
import com.prhythm.sskv002.cart.socket.vo.BookingDirective
import com.prhythm.sskv002.cart.util.logJson
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class MessageService(
    private val mapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaGroupAdvisor: KafkaGroupAdvisor,
) {

    private val log = LoggerFactory.getLogger(MessageService::class.java)

    fun sendBookingOrder(bookingDirective: BookingDirective): Mono<Void> {
        log.debug("send booking order: {}", bookingDirective.logJson())
        return bookingDirective.toMono()
            .map { mapper.writeValueAsString(it) }
            .map { kafkaTemplate.send(MessageQueue.TOPIC_CART_BOOKING, kafkaGroupAdvisor.next(MessageQueue.TOPIC_BOOKING_RESULT).toString(), it) }
            .flatMap { Mono.fromCompletionStage(it.completable()) }
            .then()
    }

}