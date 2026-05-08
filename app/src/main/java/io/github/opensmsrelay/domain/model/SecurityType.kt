package io.github.opensmsrelay.domain.model

enum class SecurityType(val displayName: String) {
    SSL_TLS("SSL/TLS"),
    STARTTLS("STARTTLS"),
    NONE("None")
}
