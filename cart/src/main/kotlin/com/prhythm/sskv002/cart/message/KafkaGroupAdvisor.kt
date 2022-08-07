package com.prhythm.sskv002.cart.message

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class KafkaGroupAdvisor {

    private val log = LoggerFactory.getLogger(KafkaGroupAdvisor::class.java)
    private val assignedPartitions: MutableMap<String, MutableSet<Int>> = ConcurrentHashMap()

    private fun obtainPartitions(topic: String): MutableSet<Int> {
        return assignedPartitions.getOrPut(topic) { CopyOnWriteArraySet() }
    }

    fun register(topic: String, partitions: IntArray) {
        synchronized(assignedPartitions) {
            obtainPartitions(topic).clear()
            for (partition in partitions) {
                register(topic, partition)
            }
        }
    }

    @Synchronized
    fun register(topic: String, partition: Int) {
        val partitions = obtainPartitions(topic)
        partitions.add(partition)
    }

    @Synchronized
    fun unregister(topic: String, partition: Int) {
        val partitions = obtainPartitions(topic)
        partitions.remove(partition)
    }

    fun next(topic: String): Int {
        val partitions = obtainPartitions(topic)
        val size: Int = partitions.size
        if (size == 0) {
            log.error("no partition available for topic: $topic")
            throw IllegalStateException()
        }
        return partitions.random()
    }
}
