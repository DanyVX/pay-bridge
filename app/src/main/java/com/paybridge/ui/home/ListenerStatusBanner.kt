package com.paybridge.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paybridge.ui.theme.PaperSurface
import com.paybridge.ui.theme.StampGreen
import com.paybridge.ui.theme.StampRed

/**
 * Persistent, impossible-to-miss status indicator — shown on every screen that hosts it, not
 * just at initial permission grant, because an OEM privacy-cleanup prompt or the shopkeeper
 * themselves can revoke access at any time.
 */
@Composable
fun ListenerStatusBanner(isEnabled: Boolean) {
    val context = LocalContext.current
    val background = if (isEnabled) StampGreen else StampRed

    Text(
        text = if (isEnabled) "Listener: ACTIVE" else "Listener: NOT RUNNING — tap to fix",
        color = PaperSurface,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .let {
                if (isEnabled) it else it.clickable {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}
