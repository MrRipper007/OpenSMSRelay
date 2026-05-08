package io.github.opensmsrelay.feature.rules

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.opensmsrelay.domain.model.SenderMatchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: RuleEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val rule = uiState.rule
    var matchTypeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (rule.id == 0L) "New Rule" else "Edit Rule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
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

            OutlinedTextField(
                value = rule.name,
                onValueChange = { viewModel.updateRule(rule.copy(name = it)) },
                label = { Text("Rule Name") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Match Type dropdown
            ExposedDropdownMenuBox(
                expanded = matchTypeExpanded,
                onExpandedChange = { matchTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = rule.matchType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Match Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matchTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = matchTypeExpanded,
                    onDismissRequest = { matchTypeExpanded = false }
                ) {
                    SenderMatchType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.updateRule(rule.copy(matchType = type))
                                matchTypeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = rule.senderValue,
                onValueChange = { viewModel.updateRule(rule.copy(senderValue = it)) },
                label = { Text("Sender Value") },
                placeholder = { Text(
                    when (rule.matchType) {
                        SenderMatchType.EXACT -> "e.g. SAMPATH, +94771234567"
                        SenderMatchType.CONTAINS -> "e.g. BANK"
                        SenderMatchType.REGEX -> "e.g. ^\\+94[0-9]{9}$"
                    }
                ) },
                isError = uiState.senderError != null || uiState.regexError != null,
                supportingText = (uiState.senderError ?: uiState.regexError)?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rule.bodyFilter ?: "",
                onValueChange = {
                    viewModel.updateRule(rule.copy(bodyFilter = it.ifBlank { null }))
                },
                label = { Text("Body Keyword Filter (optional)") },
                placeholder = { Text("e.g. OTP, your code") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Forward via Email")
                Switch(
                    checked = rule.forwardEmail,
                    onCheckedChange = { viewModel.updateRule(rule.copy(forwardEmail = it)) }
                )
            }

            AnimatedVisibility(visible = rule.forwardEmail) {
                OutlinedTextField(
                    value = uiState.emailDestinationsText,
                    onValueChange = { viewModel.updateEmailDestText(it) },
                    label = { Text("Recipient Email(s)") },
                    placeholder = { Text("e.g. user@gmail.com, other@gmail.com") },
                    isError = uiState.emailDestinationsError != null,
                    supportingText = {
                        Text(uiState.emailDestinationsError ?: "Separate multiple addresses with a comma")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Forward via SMS")
                Switch(
                    checked = rule.forwardSms,
                    onCheckedChange = { viewModel.updateRule(rule.copy(forwardSms = it)) }
                )
            }

            AnimatedVisibility(visible = rule.forwardSms) {
                OutlinedTextField(
                    value = uiState.smsDestinationsText,
                    onValueChange = { viewModel.updateSmsDestText(it) },
                    label = { Text("Forward to Number(s)") },
                    placeholder = { Text("e.g. +94771234567, +94701234567") },
                    supportingText = { Text("Separate multiple numbers with a comma") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rule Enabled")
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { viewModel.updateRule(rule.copy(isEnabled = it)) }
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Rule")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
