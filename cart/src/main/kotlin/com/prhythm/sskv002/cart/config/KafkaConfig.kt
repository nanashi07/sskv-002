package com.prhythm.sskv002.cart.config

import com.prhythm.sskv002.cart.config.vo.KafkaServerProperties
import com.prhythm.sskv002.cart.constant.MessageQueue.TOPIC_BOOKING_RESULT
import com.prhythm.sskv002.cart.constant.MessageQueue.TOPIC_CART_BOOKING
import com.prhythm.sskv002.cart.message.KafkaGroupAdvisor
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener
import java.util.stream.Collectors

@Configuration
class KafkaConfig {

    private val log = LoggerFactory.getLogger(KafkaConfig::class.java)

    @Bean
    @ConfigurationProperties("com.prhythm.sskv002.cart.kafka")
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
    fun topicBookingResult(): NewTopic {
        return NewTopic(TOPIC_BOOKING_RESULT, 10, 1.toShort())
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
    fun kafkaGroupAdvisor(): KafkaGroupAdvisor? {
        return KafkaGroupAdvisor()
    }

    @Bean
    fun consumerAwareRebalanceListener(kafkaGroupAdvisor: KafkaGroupAdvisor): ConsumerAwareRebalanceListener? {
        return object : ConsumerAwareRebalanceListener {
            override fun onPartitionsLost(consumer: Consumer<*, *>, partitions: Collection<TopicPartition>) {
                log.info("partition lost: {}", partitions)
                for (partition in partitions) {
                    kafkaGroupAdvisor.unregister(partition.topic(), partition.partition())
                }
            }

            override fun onPartitionsAssigned(consumer: Consumer<*, *>, partitions: Collection<TopicPartition>) {
                log.info("partition assigned: {}", partitions)
                partitions.stream()
                    .collect(Collectors.groupingBy { obj: TopicPartition -> obj.topic() })
                    .forEach { (topic: String?, group: List<TopicPartition>) ->
                        kafkaGroupAdvisor.register(
                            topic,
                            group.stream().mapToInt { obj: TopicPartition -> obj.partition() }.toArray()
                        )
                    }
            }
        }
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