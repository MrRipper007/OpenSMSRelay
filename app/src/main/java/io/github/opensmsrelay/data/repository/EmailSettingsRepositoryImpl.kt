package io.github.opensmsrelay.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.opensmsrelay.core.security.SecureStorage
import io.github.opensmsrelay.data.datastore.PreferenceKeys
import io.github.opensmsrelay.domain.model.EmailSettings
import io.github.opensmsrelay.domain.model.SecurityType
import io.github.opensmsrelay.domain.repository.EmailSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class EmailSettingsRepositoryImpl @Inject constructor(
    @Named("email") private val dataStore: DataStore<Preferences>,
    private val secureStorage: SecureStorage
) : EmailSettingsRepository {

    // Non-sensitive fields stay in DataStore; sensitive fields come from SecureStorage.
    override fun getFlow(): Flow<EmailSettings> = dataStore.data.map { prefs ->
        EmailSettings(
            isEnabled    = prefs[PreferenceKeys.SMTP_ENABLED] ?: false,
            host         = prefs[PreferenceKeys.SMTP_HOST] ?: "",
            port         = prefs[PreferenceKeys.SMTP_PORT] ?: 587,
            securityType = prefs[PreferenceKeys.SMTP_SECURITY]
                ?.let { runCatching { SecurityType.valueOf(it) }.getOrNull() }
                ?: SecurityType.STARTTLS,
            username     = secureStorage.getSmtpUsername(),
            password     = secureStorage.getSmtpPassword(),
            fromEmail    = secureStorage.getSmtpFromEmail()
        )
    }

    override suspend fun get(): EmailSettings = getFlow().first()

    override suspend fun save(settings: EmailSettings) {
        secureStorage.saveSmtpPassword(settings.password)
        secureStorage.saveSmtpUsername(settings.username)
        secureStorage.saveSmtpFromEmail(settings.fromEmail)
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SMTP_ENABLED]  = settings.isEnabled
            prefs[PreferenceKeys.SMTP_HOST]     = settings.host
            prefs[PreferenceKeys.SMTP_PORT]     = settings.port
            prefs[PreferenceKeys.SMTP_SECURITY] = settings.securityType.name
        }
    }
}
