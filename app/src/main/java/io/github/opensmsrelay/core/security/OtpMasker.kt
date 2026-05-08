package io.github.opensmsrelay.core.security

object OtpMasker {
    // Matches 4-8 digit standalone numbers (not part of longer digit sequences)
    private val OTP_PATTERN = Regex("""(?<!\d)(\d{4,8})(?!\d)""")

    fun mask(text: String): String = OTP_PATTERN.replace(text, "******")

    fun maskIfEnabled(text: String, enabled: Boolean): String =
        if (enabled) mask(text) else text
}
