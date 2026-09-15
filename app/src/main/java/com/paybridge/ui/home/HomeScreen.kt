package com.paybridge.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.domain.model.Provider
import com.paybridge.ui.theme.InkFaded

@Composable
fun HomeScreen(
    listenerEnabled: Boolean,
    unparsedCounts: Map<Provider, Int>,
    pendingClaims: List<ClaimEntity>,
    onNewClaim: () -> Unit,
    onClaimClick: (Long) -> Unit,
    onViewHistory: () -> Unit,
    onOpenDiagnostics: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListenerStatusBanner(isEnabled = listenerEnabled)
        UnparsedWarningBanner(unparsedCounts)

        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onNewClaim, modifier = Modifier.fillMaxWidth()) {
                Text("+ New Expected Payment")
            }
        }

        if (pendingClaims.isEmpty()) {
            Text(
                "No pending claims.",
                color = InkFaded,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            Text(
                "Pending",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(pendingClaims, key = { it.id }) { claim ->
                    PendingClaimRow(claim, onClick = { onClaimClick(claim.id) })
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(onClick = onViewHistory, modifier = Modifier.fillMaxWidth()) {
                Text("View History")
            }
            TextButton(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("Diagnostics")
            }
        }
    }
}

@Composable
private fun PendingClaimRow(claim: ClaimEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Rs. ${claim.expectedAmount}" + (claim.expectedProvider?.let { " via $it" } ?: " (any provider)"))
        TextButton(onClick = onClick) { Text("View") }
    }
}
