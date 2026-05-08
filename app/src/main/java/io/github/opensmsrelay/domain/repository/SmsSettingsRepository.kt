package io.github.opensmsrelay.domain.repository

import io.github.opensmsrelay.domain.model.SmsSettings
import kotlinx.coroutines.flow.Flow

interface SmsSettingsRepository {
    fun getFlow(): Flow<SmsSettings>
    suspend fun get(): SmsSettings
    suspend fun save(settings: SmsSettings)
}
