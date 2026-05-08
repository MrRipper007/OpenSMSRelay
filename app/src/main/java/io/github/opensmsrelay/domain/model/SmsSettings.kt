package io.github.opensmsrelay.domain.model

data class SmsSettings(
    val isEnabled: Boolean = false,
    val destinations: List<String> = emptyList()
)
