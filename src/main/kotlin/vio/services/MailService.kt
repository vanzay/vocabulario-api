package vio.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
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
}
