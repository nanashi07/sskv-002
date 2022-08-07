package com.prhythm.sskv002.activity.service

import com.prhythm.sskv002.activity.service.vo.BookShow
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class MessageManageService(private val kafkaAdmin: KafkaAdmin) {

    private val log = LoggerFactory.getLogger(MessageManageService::class.java)
    private val map = mutableMapOf<String, Any>()

    fun obtainOrCreateTopic(activityId: String, showId: String): Mono<String> {
        val topicName = "$activityId@$showId"
        return if (map.containsKey(topicName)) {
            topicName.toMono()
        } else {
            // check topic exists, create new topic if not exists
            val describeTopics = kafkaAdmin.describeTopics(topicName)
            val topicDescription = describeTopics.values.firstOrNull()
            if (topicDescription == null) {
                log.info("Create topic: $topicName")
                kafkaAdmin.createOrModifyTopics(NewTopic(topicName, 10, 1.toShort()))
                // DUMMY: add new item to activity pool and notify all activity services
            }
            map[topicName] = "" // TODO:
            topicName.toMono()
        }
    }

    fun sendCoordinate(bookShow: BookShow): Mono<Void> {

        return Mono.empty()
    }

}