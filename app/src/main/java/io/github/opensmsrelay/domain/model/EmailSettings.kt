package io.github.opensmsrelay.domain.model

data class EmailSettings(
    val isEnabled: Boolean = false,
    val host: String = "",
    val port: Int = 587,
    val securityType: SecurityType = SecurityType.STARTTLS,
    val username: String = "",
    val password: String = "",
    val fromEmail: String = "",
    val recipients: List<String> = emptyList()
)
