package com.paybridge.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shown once, on first launch. Since PayBridge is sideloaded rather than distributed through
 * Play, this screen substitutes for the trust signal Play's review would otherwise provide —
 * it must not bury the notification-access explanation in fine print (see project brief §5).
 */
@Composable
fun TrustScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("PayBridge", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Text(
            "This app is not from the Play Store. It's installed directly, because it needs a " +
                "permission (\"Notification access\") that Play Store review treats as high-risk " +
                "and is unlikely to approve for an app this small right now."
        )

        Text(
            "What PayBridge does: it reads the text of payment-received notifications from " +
                "NayaPay, Easypaisa, and JazzCash on THIS phone, to confirm a payment really " +
                "arrived before you hand over goods."
        )

        Text(
            "What it does NOT do: it never reads notifications from any other app (no WhatsApp, " +
                "no SMS, nothing else), and nothing it reads is ever sent anywhere — everything " +
                "stays on this device."
        )

        Text(
            "Next, you'll be asked to turn on Notification Access for PayBridge in Android " +
                "Settings. That's a one-time step."
        )

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}
