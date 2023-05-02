package polyflow.features.email.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import polyflow.config.PolyflowMailProperties
import polyflow.features.email.model.Email

@Service
class EmailSenderServiceImpl(
    private val javaMailSender: JavaMailSender,
    private val polyflowMailProperties: PolyflowMailProperties
) : EmailSenderService {

    override fun sendEmail(email: Email) {
        val mimeMessage = javaMailSender.createMimeMessage()

        MimeMessageHelper(mimeMessage, "utf-8").apply {
            setFrom(polyflowMailProperties.from)
            setTo(email.recipient)
            setSubject(email.subject)
            setText(email.messageBody, true)
        }

        javaMailSender.send(mimeMessage)
    }
}
