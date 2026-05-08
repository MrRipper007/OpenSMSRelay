package io.github.opensmsrelay.feature.dashboard

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.opensmsrelay.core.permissions.getPermissionState
import io.github.opensmsrelay.domain.model.AppSettings
import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.repository.AppSettingsRepository
import io.github.opensmsrelay.domain.repository.LogRepository
import io.github.opensmsrelay.domain.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val hasReceiveSmsPermission: Boolean = false,
    val hasSendSmsPermission: Boolean = false,
    val forwardingEnabled: Boolean = false,
    val activeRuleCount: Int = 0,
    val lastLog: ForwardingLog? = null,
    val appSettings: AppSettings = AppSettings(),
    val isBatteryOptimized: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsRepository: AppSettingsRepository,
    private val ruleRepository: RuleRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _permissionRefresh = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = combine(
        appSettingsRepository.getFlow(),
        ruleRepository.getAllFlow(),
        logRepository.getAllFlow(),
        _permissionRefresh
    ) { appSettings, rules, logs, _ ->
        val permState = context.getPermissionState()
        DashboardUiState(
            hasReceiveSmsPermission = permState.hasReceiveSms,
            hasSendSmsPermission = permState.hasSendSms,
            forwardingEnabled = appSettings.forwardingEnabled,
            activeRuleCount = rules.count { it.isEnabled },
            lastLog = logs.firstOrNull(),
            appSettings = appSettings,
            isBatteryOptimized = isBatteryOptimized()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun toggleForwarding(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettingsRepository.get()
            appSettingsRepository.save(current.copy(forwardingEnabled = enabled))
        }
    }

    fun refreshPermissions() {
        _permissionRefresh.value++
    }

    private fun isBatteryOptimized(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return !pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
