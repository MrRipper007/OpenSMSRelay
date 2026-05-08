package io.github.opensmsrelay.domain.repository

import io.github.opensmsrelay.domain.model.EmailSettings
import kotlinx.coroutines.flow.Flow

interface EmailSettingsRepository {
    fun getFlow(): Flow<EmailSettings>
    suspend fun get(): EmailSettings
    suspend fun save(settings: EmailSettings)
}
