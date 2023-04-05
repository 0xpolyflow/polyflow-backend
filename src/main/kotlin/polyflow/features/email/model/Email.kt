package polyflow.features.email.model

data class Email(
    val recipient: String,
    val subject: String,
    val messageBody: String
)
