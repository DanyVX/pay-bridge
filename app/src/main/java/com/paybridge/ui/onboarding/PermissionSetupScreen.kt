package com.paybridge.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paybridge.ui.theme.StampGreen
import com.paybridge.ui.theme.StampRed

/**
 * Launches the Settings screen where the shopkeeper flips on Notification Access, and re-checks
 * status on return (via [isEnabled], re-evaluated by the caller's lifecycle observer). This
 * screen is also reachable later as the "tap to fix" destination from the persistent status
 * banner — it isn't a one-time-only onboarding step.
 */
@Composable
fun PermissionSetupScreen(isEnabled: Boolean, onContinue: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Turn on Notification Access", style = MaterialTheme.typography.titleLarge)

        Text(
            "PayBridge needs permission to read notifications from your payment apps. This is " +
                "granted in Android Settings, not in this app — tap the button below to open it."
        )

        Text(
            if (isEnabled) "Status: GRANTED" else "Status: NOT GRANTED",
            color = if (isEnabled) StampGreen else StampRed,
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedButton(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open Notification Access Settings")
        }

        Button(
            onClick = onContinue,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isEnabled) "Continue" else "Waiting for permission…")
        }
    }
}
