package vio.listeners.messages

data class SendEmailMessage(val to: String, val subject: String, val text: String) {}