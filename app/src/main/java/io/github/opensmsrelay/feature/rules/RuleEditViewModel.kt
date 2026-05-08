package io.github.opensmsrelay.feature.rules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.opensmsrelay.core.common.isValidEmail
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SenderMatchType
import io.github.opensmsrelay.domain.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RuleEditUiState(
    val rule: Rule = Rule(name = "", matchType = SenderMatchType.EXACT, senderValue = ""),
    val emailDestinationsText: String = "",
    val smsDestinationsText: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val senderError: String? = null,
    val regexError: String? = null,
    val emailDestinationsError: String? = null
)

@HiltViewModel
class RuleEditViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ruleId: Long = savedStateHandle.get<Long>("ruleId") ?: -1L

    private val _uiState = MutableStateFlow(RuleEditUiState())
    val uiState: StateFlow<RuleEditUiState> = _uiState.asStateFlow()

    init {
        if (ruleId > 0) {
            viewModelScope.launch {
                val existing = ruleRepository.getById(ruleId)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(
                        rule = existing,
                        emailDestinationsText = existing.emailDestinations.joinToString(", "),
                        smsDestinationsText = existing.smsDestinations.joinToString(", ")
                    )
                }
            }
        }
    }

    fun updateRule(rule: Rule) {
        _uiState.value = _uiState.value.copy(
            rule = rule,
            nameError = null,
            senderError = null,
            regexError = null,
            emailDestinationsError = null
        )
    }

    fun updateEmailDestText(text: String) {
        _uiState.value = _uiState.value.copy(emailDestinationsText = text, emailDestinationsError = null)
    }

    fun updateSmsDestText(text: String) {
        _uiState.value = _uiState.value.copy(smsDestinationsText = text)
    }

    fun save() {
        val state = _uiState.value
        val rule = state.rule.copy(
            emailDestinations = state.emailDestinationsText
                .split(",").map { it.trim() }.filter { it.isNotEmpty() },
            smsDestinations = state.smsDestinationsText
                .split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
        var hasError = false

        if (rule.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            hasError = true
        }
        if (rule.senderValue.isBlank()) {
            _uiState.value = _uiState.value.copy(senderError = "Sender value is required")
            hasError = true
        }
        if (rule.matchType == SenderMatchType.REGEX) {
            val regexError = runCatching { Regex(rule.senderValue) }.exceptionOrNull()
            if (regexError != null) {
                _uiState.value = _uiState.value.copy(regexError = "Invalid regex: ${regexError.message}")
                hasError = true
            }
        }
        if (rule.forwardEmail) {
            val invalidEmail = rule.emailDestinations.firstOrNull { !it.isValidEmail() }
            when {
                rule.emailDestinations.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        emailDestinationsError = "At least one recipient email is required"
                    )
                    hasError = true
                }
                invalidEmail != null -> {
                    _uiState.value = _uiState.value.copy(
                        emailDestinationsError = "Invalid email: $invalidEmail"
                    )
                    hasError = true
                }
            }
        }
        if (hasError) return

        viewModelScope.launch {
            if (rule.id == 0L) {
                ruleRepository.insert(rule)
            } else {
                ruleRepository.update(rule)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
