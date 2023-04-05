package polyflow.features.email.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import polyflow.features.email.model.Email

@Service
class EmailSenderServiceImpl(
    private val javaMailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val sender: String
) : EmailSenderService {

    override fun sendEmail(email: Email) {
        val message = SimpleMailMessage().apply {
            setFrom(sender)
            setTo(email.recipient)
            setSubject(email.subject)
            setText(email.messageBody)
        }

        javaMailSender.send(message)
    }
}
