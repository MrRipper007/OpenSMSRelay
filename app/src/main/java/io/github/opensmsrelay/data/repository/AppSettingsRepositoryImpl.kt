package io.github.opensmsrelay.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.opensmsrelay.data.datastore.PreferenceKeys
import io.github.opensmsrelay.domain.model.AppSettings
import io.github.opensmsrelay.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class AppSettingsRepositoryImpl @Inject constructor(
    @Named("app") private val dataStore: DataStore<Preferences>
) : AppSettingsRepository {

    override fun getFlow(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            forwardingEnabled = prefs[PreferenceKeys.FORWARDING_ENABLED] ?: false,
            privacyMode = prefs[PreferenceKeys.PRIVACY_MODE] ?: false,
            otpMasking = prefs[PreferenceKeys.OTP_MASKING] ?: false,
            pinEnabled = prefs[PreferenceKeys.PIN_ENABLED] ?: false
        )
    }

    override suspend fun get(): AppSettings = getFlow().first()

    override suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.FORWARDING_ENABLED] = settings.forwardingEnabled
            prefs[PreferenceKeys.PRIVACY_MODE] = settings.privacyMode
            prefs[PreferenceKeys.OTP_MASKING] = settings.otpMasking
            prefs[PreferenceKeys.PIN_ENABLED] = settings.pinEnabled
        }
    }
}
