package io.github.opensmsrelay.feature.smssettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SmsSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Forward Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SMS Forwarding Enabled", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(isEnabled = it)) }
                )
            }

            OutlinedTextField(
                value = settings.destinations.joinToString(", "),
                onValueChange = {
                    viewModel.updateSettings(settings.copy(
                        destinations = it.split(",").map { n -> n.trim() }.filter { n -> n.isNotEmpty() }
                    ))
                },
                label = { Text("Destination Numbers (comma-separated)") },
                placeholder = { Text("+94771234567, +94701234567") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Saving…" else "Save Settings")
            }
            OutlinedButton(
                onClick = { viewModel.testSms() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isTesting
            ) {
                Text(if (uiState.isTesting) "Sending…" else "Send Test SMS")
            }

            Text(
                "SEND_SMS permission is required to forward SMS.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
