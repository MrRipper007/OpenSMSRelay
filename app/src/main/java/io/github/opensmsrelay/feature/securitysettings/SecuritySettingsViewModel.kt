package io.github.opensmsrelay.feature.securitysettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.opensmsrelay.domain.model.AppSettings
import io.github.opensmsrelay.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = appSettingsRepository.getFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun toggleOtpMasking(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettingsRepository.get()
            appSettingsRepository.save(current.copy(otpMasking = enabled))
        }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettingsRepository.get()
            appSettingsRepository.save(current.copy(privacyMode = enabled))
        }
    }

    fun togglePin(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettingsRepository.get()
            appSettingsRepository.save(current.copy(pinEnabled = enabled))
        }
    }
}
