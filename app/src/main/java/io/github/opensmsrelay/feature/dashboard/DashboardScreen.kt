package io.github.opensmsrelay.feature.dashboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.opensmsrelay.core.common.toReadableDateTime
import io.github.opensmsrelay.core.common.truncate
import io.github.opensmsrelay.core.designsystem.components.PermissionBanner
import io.github.opensmsrelay.core.designsystem.components.StatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToRules: () -> Unit,
    onNavigateToEmailSettings: () -> Unit,
    onNavigateToSmsSettings: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.refreshPermissions() }

    val batteryOptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshPermissions() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Open SMS Relay") })
        }
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

            if (!uiState.hasReceiveSmsPermission) {
                PermissionBanner(
                    message = "RECEIVE_SMS permission is required to intercept incoming messages.",
                    actionLabel = "Grant",
                    onAction = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
                        )
                    }
                )
            }

            if (uiState.isBatteryOptimized) {
                PermissionBanner(
                    message = "Battery optimisation is active. Android may delay or stop SMS forwarding. Tap to disable it for this app.",
                    actionLabel = "Fix",
                    onAction = {
                        batteryOptLauncher.launch(
                            Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                )
            }

            // Forwarding toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Forwarding Enabled", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.forwardingEnabled,
                    onCheckedChange = { viewModel.toggleForwarding(it) },
                    enabled = uiState.hasReceiveSmsPermission
                )
            }

            // Status cards
            StatusCard(
                title = "SMS Permission",
                value = if (uiState.hasReceiveSmsPermission) "Granted" else "Not granted",
                icon = Icons.Default.Sms,
                iconTint = if (uiState.hasReceiveSmsPermission)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            StatusCard(
                title = "Active Rules",
                value = uiState.activeRuleCount.toString(),
                icon = Icons.Default.Rule,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            uiState.lastLog?.let { log ->
                StatusCard(
                    title = "Last SMS",
                    value = "${log.sender} — ${log.timestamp.toReadableDateTime()}",
                    icon = Icons.Default.Phone
                )
                StatusCard(
                    title = "Last Forwarding",
                    value = "Email: ${log.emailStatus.name}  SMS: ${log.smsStatus.name}",
                    icon = Icons.Default.Email,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Navigation", style = MaterialTheme.typography.titleSmall)

            Button(onClick = onNavigateToRules, modifier = Modifier.fillMaxWidth()) {
                Text("Forwarding Rules")
            }
            Button(onClick = onNavigateToEmailSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Email Settings")
            }
            Button(onClick = onNavigateToSmsSettings, modifier = Modifier.fillMaxWidth()) {
                Text("SMS Forward Settings")
            }
            OutlinedButton(onClick = onNavigateToLogs, modifier = Modifier.fillMaxWidth()) {
                Text("View Logs")
            }
            OutlinedButton(onClick = onNavigateToSecurity, modifier = Modifier.fillMaxWidth()) {
                Text("Security Settings")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
