package io.github.opensmsrelay.domain.repository

import io.github.opensmsrelay.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getFlow(): Flow<AppSettings>
    suspend fun get(): AppSettings
    suspend fun save(settings: AppSettings)
}
