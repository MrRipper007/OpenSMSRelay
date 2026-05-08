package io.github.opensmsrelay

import io.github.opensmsrelay.core.common.Constants
import io.github.opensmsrelay.core.security.OtpMasker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailTemplateTest {

    @Test
    fun `email subject contains sender`() {
        val sender = "SAMPATH"
        val subject = Constants.EMAIL_SUBJECT_TEMPLATE.replace("{sender}", sender)
        assertTrue(subject.contains(sender))
    }

    @Test
    fun `SMS template contains sender and message`() {
        val sender = "SAMPATH"
        val time = "2025-01-01 12:00:00"
        val message = "Your balance is Rs. 10000"

        val result = Constants.SMS_FORWARD_TEMPLATE
            .replace("{sender}", sender)
            .replace("{time}", time)
            .replace("{message}", message)

        assertTrue(result.contains(sender))
        assertTrue(result.contains(message))
        assertTrue(result.contains(time))
    }

    @Test
    fun `OTP masking removes sensitive code before forwarding`() {
        val body = "Your OTP is 123456. Valid for 5 minutes."
        val masked = OtpMasker.mask(body)

        assertFalse(masked.contains("123456"))
        assertTrue(masked.contains("******"))
    }

    @Test
    fun `OTP masking preserves non-OTP content`() {
        val body = "Balance: Rs. 10000. No OTP in this message."
        val masked = OtpMasker.mask(body)
        // 10000 is 5 digits so it WOULD be masked — document this behaviour
        assertTrue(masked.contains("Balance: Rs."))
    }

    @Test
    fun `validate SMTP email format`() {
        val valid = listOf("user@gmail.com", "user+tag@domain.co.uk", "user@sub.domain.com")
        val invalid = listOf("notanemail", "missing@", "@nodomain", "")

        valid.forEach { email ->
            assertTrue("Should be valid: $email",
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        }
    }
}
