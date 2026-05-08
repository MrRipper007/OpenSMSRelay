package io.github.opensmsrelay.domain.model

data class SmsMessage(
    val sender: String,
    val body: String,
    val receivedAt: Long = System.currentTimeMillis()
)
