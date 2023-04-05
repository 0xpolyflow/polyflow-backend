package polyflow.features.email.service

import polyflow.features.email.model.Email

interface EmailSenderService {
    fun sendEmail(email: Email)
}
