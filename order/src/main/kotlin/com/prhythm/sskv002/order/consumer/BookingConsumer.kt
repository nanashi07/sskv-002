package com.prhythm.sskv002.order.consumer

import com.prhythm.sskv002.order.constant.MessageQueue.TOPIC_CART_BOOKING
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class BookingConsumer {

    private val log = LoggerFactory.getLogger(BookingConsumer::class.java)

    @KafkaListener(topics = [TOPIC_CART_BOOKING], groupId = "sskv-002-order")
    fun accept(
        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) key: Int?,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Payload message: String?
    ) {
        log.debug("consumed message: {}", message)
    }
}