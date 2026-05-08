package io.github.opensmsrelay.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── SMTP credentials ────────────────────────────────────────────────────

    fun saveSmtpPassword(password: String) = put(KEY_SMTP_PASSWORD, password)
    fun getSmtpPassword(): String = get(KEY_SMTP_PASSWORD)

    fun saveSmtpUsername(username: String) = put(KEY_SMTP_USERNAME, username)
    fun getSmtpUsername(): String = get(KEY_SMTP_USERNAME)

    fun saveSmtpFromEmail(from: String) = put(KEY_SMTP_FROM, from)
    fun getSmtpFromEmail(): String = get(KEY_SMTP_FROM)

    // ── SMS destinations ─────────────────────────────────────────────────────

    fun saveSmsDestinations(destinations: List<String>) = put(KEY_SMS_DESTINATIONS, destinations.joinToString(","))
    fun getSmsDestinations(): List<String> = get(KEY_SMS_DESTINATIONS)
        .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // ── Database passphrase ──────────────────────────────────────────────────

    /**
     * Returns the AES-256 passphrase for SQLCipher, generating and persisting it
     * on the first call. The key never leaves EncryptedSharedPreferences.
     */
    fun getDatabasePassphrase(): ByteArray {
        var encoded = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (encoded == null) {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            prefs.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()
        }
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun clearAll() = prefs.edit().clear().apply()

    private fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    private fun get(key: String): String = prefs.getString(key, "") ?: ""

    // ── Keys ─────────────────────────────────────────────────────────────────

    companion object {
        private const val KEY_SMTP_PASSWORD    = "smtp_password"
        private const val KEY_SMTP_USERNAME    = "smtp_username"
        private const val KEY_SMTP_FROM        = "smtp_from"
        private const val KEY_SMS_DESTINATIONS = "sms_destinations"
        private const val KEY_DB_PASSPHRASE    = "db_passphrase"
    }
}
