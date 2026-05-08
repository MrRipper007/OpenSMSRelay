package io.github.opensmsrelay.domain.model

data class Rule(
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val matchType: SenderMatchType,
    val senderValue: String,
    val bodyFilter: String? = null,
    val forwardEmail: Boolean = true,
    val forwardSms: Boolean = false,
    val emailDestinations: List<String> = emptyList(),
    val smsDestinations: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
