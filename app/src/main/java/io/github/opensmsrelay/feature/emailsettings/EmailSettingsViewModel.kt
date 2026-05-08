package io.github.opensmsrelay.feature.emailsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.domain.model.EmailSettings
import io.github.opensmsrelay.domain.repository.EmailSettingsRepository
import io.github.opensmsrelay.domain.usecase.ValidateSmtpSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailSettingsUiState(
    val settings: EmailSettings = EmailSettings(),
    val isSaving: Boolean = false,
    val testResult: String? = null,
    val saveError: String? = null
)

@HiltViewModel
class EmailSettingsViewModel @Inject constructor(
    private val emailSettingsRepository: EmailSettingsRepository,
    private val validateSmtpSettingsUseCase: ValidateSmtpSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailSettingsUiState())
    val uiState: StateFlow<EmailSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = emailSettingsRepository.get()
            _uiState.value = _uiState.value.copy(settings = settings)
        }
    }

    fun updateSettings(settings: EmailSettings) {
        _uiState.value = _uiState.value.copy(settings = settings, saveError = null, testResult = null)
    }

    fun save() {
        val settings = _uiState.value.settings
        when (val validation = validateSmtpSettingsUseCase(settings)) {
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(saveError = validation.message)
                return
            }
            else -> {}
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            emailSettingsRepository.save(settings)
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }
}
