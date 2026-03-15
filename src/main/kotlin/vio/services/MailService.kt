package vio.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import vio.listeners.messages.MessageType
import vio.listeners.messages.SendEmailMessage

@Service
class MailService(
    private val queueService: QueueService,
    private val mailSender: JavaMailSender,
    @Value("\${mail.from}")
    private val from: String
) {

    fun send(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setFrom(from)
        message.setTo(to)
        message.setSubject(subject)
        message.setText(text)
        mailSender.send(message)
    }

    fun sendWithQueue(to: String, subject: String, text: String) {
        val sendEmailMessage = SendEmailMessage(to, subject, text)
        queueService.send("email.queue", MessageType.SEND_EMAIL, sendEmailMessage)
    }
}
