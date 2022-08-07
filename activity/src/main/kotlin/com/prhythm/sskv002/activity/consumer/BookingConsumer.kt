package com.prhythm.sskv002.activity.consumer

import com.prhythm.sskv002.activity.constant.MessageQueue.TOPIC_CART_BOOKING
import com.prhythm.sskv002.activity.service.ShowService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class BookingConsumer(private val showService: ShowService) {

    private val log = LoggerFactory.getLogger(BookingConsumer::class.java)

    @KafkaListener(topics = [TOPIC_CART_BOOKING], groupId = "sskv-002-activity")
    fun accept(
        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) key: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Payload message: String
    ) {
        log.debug("consumed message: {}", message)
        showService.createBooking(message, key)
            .onErrorResume { e ->
                log.error("handle message error: {}", message, e)
                Mono.error(e)
            }
            .subscribeOn(Schedulers.immediate())
            .subscribe()
    }
}