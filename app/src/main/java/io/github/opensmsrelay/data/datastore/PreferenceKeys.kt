package io.github.opensmsrelay.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    // Email settings — non-sensitive config only.
    // username, password, fromEmail -> SecureStorage (EncryptedSharedPreferences)
    val SMTP_HOST     = stringPreferencesKey("smtp_host")
    val SMTP_PORT     = intPreferencesKey("smtp_port")
    val SMTP_SECURITY = stringPreferencesKey("smtp_security")
    val SMTP_ENABLED  = booleanPreferencesKey("smtp_enabled")

    // SMS settings — non-sensitive config only.
    // destinations → SecureStorage (EncryptedSharedPreferences)
    val SMS_ENABLED = booleanPreferencesKey("sms_enabled")

    // App settings
    val FORWARDING_ENABLED = booleanPreferencesKey("forwarding_enabled")
    val PRIVACY_MODE       = booleanPreferencesKey("privacy_mode")
    val OTP_MASKING        = booleanPreferencesKey("otp_masking")
    val PIN_ENABLED        = booleanPreferencesKey("pin_enabled")
}
