package io.github.opensmsrelay.feature.logs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.opensmsrelay.core.common.Constants
import io.github.opensmsrelay.core.common.toReadableDateTime
import io.github.opensmsrelay.core.common.truncate
import io.github.opensmsrelay.core.designsystem.components.ConfirmDialog
import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.exportFilePath) {
        uiState.exportFilePath?.let { path ->
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Logs"))
            viewModel.clearExportPath()
        }
    }

    if (uiState.showClearConfirm) {
        ConfirmDialog(
            title = "Clear All Logs",
            message = "This will permanently delete all logs. Continue?",
            confirmLabel = "Clear",
            onConfirm = { viewModel.clearLogs() },
            onDismiss = { viewModel.dismissClearConfirm() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.requestClearLogs() }) {
                        Icon(Icons.Default.Delete, "Clear logs")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.exportLogs() }) {
                Icon(Icons.Default.Share, "Export CSV")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LogFilter.entries) { filter ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }

            if (uiState.logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No logs found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        LogCard(log = log, onRetry = { viewModel.retryFailedEmail(log) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: ForwardingLog, onRetry: () -> Unit) {
    val isFailed = log.emailStatus == ForwardingStatus.FAILED || log.smsStatus == ForwardingStatus.FAILED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFailed)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else if (log.isMatched)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    log.sender,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    log.timestamp.toReadableDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                log.body.truncate(120),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            log.matchedRuleName?.let {
                Text("Rule: $it", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Email: ${log.emailStatus.name}", style = MaterialTheme.typography.labelSmall)
                Text("SMS: ${log.smsStatus.name}", style = MaterialTheme.typography.labelSmall)
            }
            log.errorMessage?.let {
                Text(
                    "Error: ${it.truncate(80)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
