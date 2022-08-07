package com.prhythm.sskv002.cart.consumer

import com.prhythm.sskv002.cart.constant.MessageQueue.TOPIC_BOOKING_RESULT
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class BookClearConsumer {

    private val log = LoggerFactory.getLogger(BookClearConsumer::class.java)

    @KafkaListener(topics = [TOPIC_BOOKING_RESULT], groupId = "sskv-002-cart")
    fun accept(
        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) key: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Payload message: String
    ) {
        log.debug("consumed message: {}", message)

    }

}