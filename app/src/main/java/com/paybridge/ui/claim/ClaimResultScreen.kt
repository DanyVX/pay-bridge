package com.paybridge.ui.claim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.ui.common.StampBadge
import com.paybridge.ui.theme.InkFaded
import com.paybridge.ui.theme.StampAmber
import com.paybridge.ui.theme.StampBlue
import com.paybridge.ui.theme.StampGreen
import com.paybridge.ui.theme.StampGrey
import com.paybridge.ui.theme.StampRed
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The core "receipt" screen: a rotated stamp for whichever ClaimStatus the claim is currently
 * in. [onTick] is invoked roughly every 10s while the claim is still PENDING — the primary,
 * timely path for resolving a timeout while this screen is open (a WorkManager backstop covers
 * the case where the app is closed instead, at a much coarser 15-minute floor).
 */
@Composable
fun ClaimResultScreen(claim: ClaimEntity?, onTick: suspend () -> Unit) {
    LaunchedEffect(claim?.id, claim?.status) {
        while (claim?.status == ClaimStatus.PENDING.name) {
            delay(10_000)
            onTick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (claim == null) {
            Text("Claim not found.")
            return@Column
        }

        Text(
            "Rs. ${claim.expectedAmount}" + (claim.expectedProvider?.let { " via $it" } ?: " (any provider)"),
            style = MaterialTheme.typography.titleMedium,
        )

        when (ClaimStatus.valueOf(claim.status)) {
            ClaimStatus.PENDING -> StampBadge(
                "NOT YET RECEIVED",
                StampBlue,
            )
            ClaimStatus.RECEIVED -> StampBadge(
                "PAYMENT RECEIVED — Rs. ${claim.matchedAmount} via ${claim.matchedProvider}",
                StampGreen,
            )
            ClaimStatus.MISMATCH -> StampBadge(
                "RECEIVED Rs. ${claim.matchedAmount} — does NOT match expected Rs. ${claim.expectedAmount}",
                StampAmber,
            )
            ClaimStatus.TIMED_OUT -> StampBadge(
                "TIMED OUT — not confirmed",
                StampRed,
            )
            ClaimStatus.COULD_NOT_VERIFY -> StampBadge(
                "COULD NOT VERIFY — listener was inactive",
                StampGrey,
            )
        }

        if (claim.status == ClaimStatus.PENDING.name) {
            Text(
                "Waiting until ${formatTime(claim.timeoutAt)}…",
                color = InkFaded,
            )
        }

        if (claim.wasAmbiguousMatch && claim.ambiguityNote != null) {
            Text(claim.ambiguityNote, color = InkFaded, style = MaterialTheme.typography.bodyMedium)
        }

        if (claim.status == ClaimStatus.RECEIVED.name || claim.status == ClaimStatus.MISMATCH.name) {
            Text(
                "Raw notification: ${claim.matchedRawText}",
                color = InkFaded,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun formatTime(epochMillis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
