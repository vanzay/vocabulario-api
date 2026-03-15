package vio.services

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import vio.listeners.messages.MessageType
import vio.listeners.messages.QueueMessage

@Service
class QueueService(
    private val redisTemplate: RedisTemplate<String?, Any?>
) {

    fun parse(message: String): QueueMessage {
        val mapper = ObjectMapper()
        return mapper.readValue(message, QueueMessage::class.java)
    }

    fun send(queueName: String, messageType: MessageType, message: Any) {
        val message = buildMessage(messageType, message)
        redisTemplate.convertAndSend(queueName, message)
    }

    private fun buildMessage(type: MessageType, message: Any): String {
        val mapper = ObjectMapper()
        val payload = mapper.writeValueAsString(message)
        val queueMessage = QueueMessage(type, payload)
        return mapper.writeValueAsString(queueMessage)
    }
}