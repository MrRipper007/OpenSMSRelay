package io.github.opensmsrelay.feature.emailsettings

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.opensmsrelay.domain.model.SecurityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmailSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val snackbarHostState = remember { SnackbarHostState() }
    var securityExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTestResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Settings") },
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
                Text("Email Forwarding Enabled", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(isEnabled = it)) }
                )
            }

            OutlinedTextField(
                value = settings.host,
                onValueChange = { viewModel.updateSettings(settings.copy(host = it)) },
                label = { Text("SMTP Host") },
                placeholder = { Text("smtp.gmail.com") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = if (settings.port == 0) "" else settings.port.toString(),
                onValueChange = {
                    viewModel.updateSettings(settings.copy(port = it.toIntOrNull() ?: 587))
                },
                label = { Text("SMTP Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = securityExpanded,
                onExpandedChange = { securityExpanded = it }
            ) {
                OutlinedTextField(
                    value = settings.securityType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Security Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = securityExpanded,
                    onDismissRequest = { securityExpanded = false }
                ) {
                    SecurityType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                viewModel.updateSettings(settings.copy(securityType = type))
                                securityExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = settings.username,
                onValueChange = { viewModel.updateSettings(settings.copy(username = it)) },
                label = { Text("Username / Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = settings.password,
                onValueChange = { viewModel.updateSettings(settings.copy(password = it)) },
                label = { Text("SMTP / App Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = settings.fromEmail,
                onValueChange = { viewModel.updateSettings(settings.copy(fromEmail = it)) },
                label = { Text("From Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            uiState.saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving) {
                Text(if (uiState.isSaving) "Saving…" else "Save Settings")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
