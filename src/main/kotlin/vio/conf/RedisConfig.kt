package vio.conf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.StringRedisSerializer
import vio.listeners.MessageListener
import vio.services.MailService
import vio.services.QueueService


@Configuration
class RedisConfig {

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)

        container.addMessageListener(listenerAdapter, ChannelTopic.of("email.queue"))

        return container
    }

    @Bean
    fun listenerAdapter(messageListener: MessageListener): MessageListenerAdapter {
        val messageListenerAdapter = MessageListenerAdapter(messageListener, "onMessage")
        messageListenerAdapter.setSerializer(StringRedisSerializer())
        messageListenerAdapter.afterPropertiesSet()
        return messageListenerAdapter
    }

    @Bean
    fun messageListener(queueService: QueueService, mailService: MailService): MessageListener {
        return MessageListener(queueService, mailService)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(connectionFactory)
        redisTemplate.setValueSerializer(StringRedisSerializer())
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }
}