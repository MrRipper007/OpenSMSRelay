package io.github.opensmsrelay.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.opensmsrelay.core.security.SecureStorage
import io.github.opensmsrelay.data.datastore.PreferenceKeys
import io.github.opensmsrelay.domain.model.SmsSettings
import io.github.opensmsrelay.domain.repository.SmsSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class SmsSettingsRepositoryImpl @Inject constructor(
    @Named("sms") private val dataStore: DataStore<Preferences>,
    private val secureStorage: SecureStorage
) : SmsSettingsRepository {

    // isEnabled stays in DataStore; destination phone numbers go to SecureStorage.
    override fun getFlow(): Flow<SmsSettings> = dataStore.data.map { prefs ->
        SmsSettings(
            isEnabled    = prefs[PreferenceKeys.SMS_ENABLED] ?: false,
            destinations = secureStorage.getSmsDestinations()
        )
    }

    override suspend fun get(): SmsSettings = getFlow().first()

    override suspend fun save(settings: SmsSettings) {
        secureStorage.saveSmsDestinations(settings.destinations)
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.SMS_ENABLED] = settings.isEnabled
        }
    }
}
