package com.paybridge.ui.claim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paybridge.domain.model.Provider
import java.util.concurrent.TimeUnit

/** Default window a claim waits before timing out — configurable per-claim in this screen. */
val DEFAULT_TIMEOUT_MINUTES = 7L

@Composable
fun NewClaimScreen(onStartWaiting: (amount: Long, provider: Provider?, timeoutMillis: Long) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var timeoutMinutesText by remember { mutableStateOf(DEFAULT_TIMEOUT_MINUTES.toString()) }

    val amount = amountText.toLongOrNull()
    val timeoutMinutes = timeoutMinutesText.toLongOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("New Expected Payment", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter(Char::isDigit) },
            label = { Text("Expected amount (Rs.)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            Text("Provider (optional)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                FilterChip(
                    selected = selectedProvider == null,
                    onClick = { selectedProvider = null },
                    label = { Text("Any") },
                )
                Provider.entries.forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = { selectedProvider = provider },
                        label = { Text(provider.name) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = timeoutMinutesText,
            onValueChange = { timeoutMinutesText = it.filter(Char::isDigit) },
            label = { Text("Timeout (minutes)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val validAmount = amount ?: return@Button
                val validTimeout = timeoutMinutes ?: return@Button
                onStartWaiting(validAmount, selectedProvider, TimeUnit.MINUTES.toMillis(validTimeout))
            },
            enabled = amount != null && amount > 0 && timeoutMinutes != null && timeoutMinutes > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Waiting")
        }
    }
}
