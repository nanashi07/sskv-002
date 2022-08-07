package com.prhythm.sskv002.order.config

import com.prhythm.sskv002.order.config.vo.KafkaServerProperties
import com.prhythm.sskv002.order.constant.MessageQueue.TOPIC_CART_BOOKING
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*

@Configuration
class KafkaConfig {

    @Bean
    @ConfigurationProperties("com.prhythm.sskv002.order.kafka")
    fun kafkaServerProperties(): KafkaServerProperties {
        return KafkaServerProperties()
    }

    @Bean
    fun kafkaAdmin(kafkaServerProperties: KafkaServerProperties): KafkaAdmin {
        val config = mapOf<String, Any>(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServerProperties.bootstrapAddress
        )
        return KafkaAdmin(config)
    }

    @Bean
    fun topicCartBooking(): NewTopic {
        return NewTopic(TOPIC_CART_BOOKING, 10, 1.toShort())
    }

    @Bean
    fun producerFactory(kafkaServerProperties: KafkaServerProperties): ProducerFactory<String, String> {
        val config = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServerProperties.bootstrapAddress,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun consumerFactory(kafkaServerProperties: KafkaServerProperties): ConsumerFactory<String, String> {
        val config = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServerProperties.bootstrapAddress,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun concurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, String>): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = consumerFactory
        }
    }

}