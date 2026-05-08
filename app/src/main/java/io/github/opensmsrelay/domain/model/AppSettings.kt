package io.github.opensmsrelay.domain.model

data class AppSettings(
    val forwardingEnabled: Boolean = false,
    val privacyMode: Boolean = false,
    val otpMasking: Boolean = false,
    val pinEnabled: Boolean = false
)
