package io.github.opensmsrelay.feature.smssettings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.domain.model.SmsSettings
import io.github.opensmsrelay.domain.repository.SmsSettingsRepository
import io.github.opensmsrelay.forwarding.SmsForwarder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsSettingsUiState(
    val settings: SmsSettings = SmsSettings(),
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null
)

@HiltViewModel
class SmsSettingsViewModel @Inject constructor(
    private val smsSettingsRepository: SmsSettingsRepository,
    private val smsForwarder: SmsForwarder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsSettingsUiState())
    val uiState: StateFlow<SmsSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = smsSettingsRepository.get()
            _uiState.value = _uiState.value.copy(settings = settings)
        }
    }

    fun updateSettings(settings: SmsSettings) {
        _uiState.value = _uiState.value.copy(settings = settings, testResult = null)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            smsSettingsRepository.save(_uiState.value.settings)
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun testSms() {
        val settings = _uiState.value.settings
        if (settings.destinations.isEmpty()) {
            _uiState.value = _uiState.value.copy(testResult = "No destination numbers configured")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, testResult = null)
            val testMessage = SmsMessage(sender = "Test", body = "Test from Open SMS Relay")
            when (val result = smsForwarder.send(testMessage, settings.destinations, testMessage.body)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(testResult = "Test SMS sent!")
                is Result.Error -> _uiState.value = _uiState.value.copy(testResult = "Failed: ${result.message}")
                else -> {}
            }
            _uiState.value = _uiState.value.copy(isTesting = false)
        }
    }
}
