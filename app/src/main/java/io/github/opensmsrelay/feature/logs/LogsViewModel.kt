package io.github.opensmsrelay.feature.logs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.repository.EmailSettingsRepository
import io.github.opensmsrelay.domain.repository.LogRepository
import io.github.opensmsrelay.domain.usecase.ExportLogsUseCase
import io.github.opensmsrelay.forwarding.ForwardingEngine
import io.github.opensmsrelay.domain.model.SmsMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class LogsUiState(
    val logs: List<ForwardingLog> = emptyList(),
    val filter: LogFilter = LogFilter.ALL,
    val showClearConfirm: Boolean = false,
    val exportFilePath: String? = null
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val exportLogsUseCase: ExportLogsUseCase,
    private val forwardingEngine: ForwardingEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _filter = MutableStateFlow(LogFilter.ALL)
    private val _showClearConfirm = MutableStateFlow(false)
    private val _exportFilePath = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LogsUiState> = combine(
        logRepository.getAllFlow(),
        _filter,
        _showClearConfirm,
        _exportFilePath
    ) { logs, filter, showClear, exportPath ->
        val filtered = when (filter) {
            LogFilter.ALL -> logs
            LogFilter.MATCHED -> logs.filter { it.isMatched }
            LogFilter.IGNORED -> logs.filter { !it.isMatched }
            LogFilter.FAILED -> logs.filter {
                it.emailStatus == ForwardingStatus.FAILED ||
                    it.smsStatus == ForwardingStatus.FAILED
            }
        }
        LogsUiState(
            logs = filtered,
            filter = filter,
            showClearConfirm = showClear,
            exportFilePath = exportPath
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LogsUiState()
    )

    fun setFilter(filter: LogFilter) {
        _filter.value = filter
    }

    fun requestClearLogs() {
        _showClearConfirm.value = true
    }

    fun dismissClearConfirm() {
        _showClearConfirm.value = false
    }

    fun clearLogs() {
        viewModelScope.launch {
            logRepository.deleteAll()
            _showClearConfirm.value = false
        }
    }

    fun exportLogs() {
        viewModelScope.launch {
            val csv = exportLogsUseCase()
            val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
            val file = File(exportDir, "opensmsrelay_logs.csv")
            file.writeText(csv)
            _exportFilePath.value = file.absolutePath
        }
    }

    fun clearExportPath() {
        _exportFilePath.value = null
    }

    fun retryFailedEmail(log: ForwardingLog) {
        viewModelScope.launch {
            val message = SmsMessage(
                sender = log.sender,
                body = log.body,
                receivedAt = log.timestamp
            )
            // Re-process through ForwardingEngine with updated status
            logRepository.update(log.copy(emailStatus = ForwardingStatus.PENDING, retryCount = log.retryCount + 1))
        }
    }
}
