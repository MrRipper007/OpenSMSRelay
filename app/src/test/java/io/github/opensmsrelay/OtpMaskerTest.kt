package io.github.opensmsrelay

import io.github.opensmsrelay.core.security.OtpMasker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpMaskerTest {

    @Test
    fun `4-digit number is masked`() {
        val result = OtpMasker.mask("Your OTP is 1234")
        assertEquals("Your OTP is ******", result)
    }

    @Test
    fun `6-digit number is masked`() {
        val result = OtpMasker.mask("Code: 123456")
        assertEquals("Code: ******", result)
    }

    @Test
    fun `8-digit number is masked`() {
        val result = OtpMasker.mask("Token: 12345678")
        assertEquals("Token: ******", result)
    }

    @Test
    fun `3-digit number is NOT masked`() {
        val result = OtpMasker.mask("Dial 123 for help")
        assertEquals("Dial 123 for help", result)
    }

    @Test
    fun `9-digit number is NOT masked`() {
        val result = OtpMasker.mask("Call 123456789")
        assertEquals("Call 123456789", result)
    }

    @Test
    fun `multiple OTPs in message are all masked`() {
        val result = OtpMasker.mask("First: 1234 Second: 5678")
        assertEquals("First: ****** Second: ******", result)
    }

    @Test
    fun `maskIfEnabled returns original when disabled`() {
        val text = "Your OTP is 1234"
        val result = OtpMasker.maskIfEnabled(text, enabled = false)
        assertEquals(text, result)
    }

    @Test
    fun `maskIfEnabled masks when enabled`() {
        val result = OtpMasker.maskIfEnabled("OTP: 123456", enabled = true)
        assertEquals("OTP: ******", result)
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", OtpMasker.mask(""))
    }

    @Test
    fun `message with no numbers unchanged`() {
        val text = "Hello World"
        assertEquals(text, OtpMasker.mask(text))
    }
}
