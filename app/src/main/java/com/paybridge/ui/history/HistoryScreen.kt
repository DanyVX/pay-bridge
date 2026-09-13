package com.paybridge.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.domain.model.Provider
import com.paybridge.ui.home.UnparsedWarningBanner
import com.paybridge.ui.theme.InkFaded
import com.paybridge.ui.theme.StampAmber
import com.paybridge.ui.theme.StampBlue
import com.paybridge.ui.theme.StampGreen
import com.paybridge.ui.theme.StampGrey
import com.paybridge.ui.theme.StampRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The full ledger, most-recent-first — every claim regardless of outcome. Also hosts the
 * unparsed-notification warning banner, since this is (with HomeScreen) one of the two
 * "ambient/always visible" screens where a shopkeeper is likely to notice it.
 *
 * On-device storage is the whole design for v1: history is lost on uninstall or device change.
 * That's a real, deliberate limitation, not a bug — the callout below says so plainly rather
 * than letting a shopkeeper discover it by surprise.
 */
@Composable
fun HistoryScreen(claims: List<ClaimEntity>, unparsedCounts: Map<Provider, Int>, onClaimClick: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        UnparsedWarningBanner(unparsedCounts)

        Text(
            "Stored only on this device — history is lost if the app is uninstalled or the phone is changed.",
            color = InkFaded,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(16.dp),
        )

        if (claims.isEmpty()) {
            Text("No history yet.", color = InkFaded, modifier = Modifier.padding(16.dp))
            return@Column
        }

        LazyColumn {
            items(claims, key = { it.id }) { claim ->
                HistoryRow(claim, onClick = { onClaimClick(claim.id) })
                Divider()
            }
        }
    }
}

@Composable
private fun HistoryRow(claim: ClaimEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Rs. ${claim.expectedAmount}" + (claim.expectedProvider?.let { " via $it" } ?: " (any provider)"))
            Text(formatTime(claim.createdAt), color = InkFaded, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            claim.status,
            color = colorForStatus(claim.status),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

private fun colorForStatus(status: String) = when (ClaimStatus.valueOf(status)) {
    ClaimStatus.PENDING -> StampBlue
    ClaimStatus.RECEIVED -> StampGreen
    ClaimStatus.MISMATCH -> StampAmber
    ClaimStatus.TIMED_OUT -> StampRed
    ClaimStatus.COULD_NOT_VERIFY -> StampGrey
}

private fun formatTime(epochMillis: Long): String =
    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
