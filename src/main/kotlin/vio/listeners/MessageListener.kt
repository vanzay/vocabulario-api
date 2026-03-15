package vio.listeners

import org.slf4j.LoggerFactory
import tools.jackson.databind.ObjectMapper
import vio.listeners.messages.MessageType
import vio.listeners.messages.SendEmailMessage
import vio.services.MailService
import vio.services.QueueService

class MessageListener(
    private val queueService: QueueService,
    private val mailService: MailService
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun onMessage(message: String) {
        log.info("Received message {}", message)

        val queueMessage = queueService.parse(message)
        when (queueMessage.type) {
            MessageType.SEND_EMAIL -> sendEmail(queueMessage.payload)
        }
    }

    fun sendEmail(message: String) {
        try {
            val mapper = ObjectMapper()
            val sendEmailMessage = mapper.readValue(message, SendEmailMessage::class.java)
            mailService.send(
                sendEmailMessage.to,
                sendEmailMessage.subject,
                sendEmailMessage.text
            )
        } catch (ex: Exception) {
            log.error("Failed to send email", ex)
        }
    }
}