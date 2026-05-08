package io.github.opensmsrelay.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.repository.RuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    val rules: StateFlow<List<Rule>> = ruleRepository.getAllFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteRule(rule: Rule) {
        viewModelScope.launch { ruleRepository.delete(rule) }
    }

    fun toggleRule(rule: Rule) {
        viewModelScope.launch {
            ruleRepository.update(rule.copy(isEnabled = !rule.isEnabled))
        }
    }
}
