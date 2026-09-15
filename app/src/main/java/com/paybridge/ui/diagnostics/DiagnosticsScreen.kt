package com.paybridge.ui.diagnostics

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paybridge.BuildConfig
import com.paybridge.data.notification.PaymentAppAllowlist
import com.paybridge.domain.model.Provider
import com.paybridge.ui.theme.InkFaded
import com.paybridge.ui.theme.StampGreen
import com.paybridge.ui.theme.StampRed

/**
 * A self-diagnostic screen for support across many independently-deployed phones. With no
 * backend, there's no way to know a shop's install is broken except them saying so — this
 * screen gives a shopkeeper (or whoever is helping them over the phone) something concrete to
 * read out: is the listener active, is each payment app actually installed under the package
 * name PayBridge expects, and how many notifications have failed to parse recently.
 */
@Composable
fun DiagnosticsScreen(listenerEnabled: Boolean, unparsedCounts: Map<Provider, Int>) {
    val context = LocalContext.current
    val installedStatus = remember {
        PaymentAppAllowlist.PACKAGE_TO_PROVIDER.mapValues { (packageName, _) ->
            isPackageInstalled(context.packageManager, packageName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.titleLarge)

        Text("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", color = InkFaded)

        Divider()

        Text("Notification listener", style = MaterialTheme.typography.titleMedium)
        Text(
            if (listenerEnabled) "ACTIVE" else "NOT RUNNING",
            color = if (listenerEnabled) StampGreen else StampRed,
        )

        Divider()

        Text("Payment apps on this phone", style = MaterialTheme.typography.titleMedium)
        Text(
            "PayBridge only recognizes specific package names for each provider — if a real " +
                "app is installed under a different package name than expected, notifications " +
                "from it won't be seen at all. See /docs/notification-samples.md.",
            color = InkFaded,
            style = MaterialTheme.typography.labelSmall,
        )
        PaymentAppAllowlist.PACKAGE_TO_PROVIDER.forEach { (packageName, provider) ->
            val installed = installedStatus[packageName] == true
            Text(
                "$provider ($packageName): " + if (installed) "installed" else "NOT installed under this package name",
                color = if (installed) StampGreen else StampRed,
            )
        }

        Divider()

        Text("Unparsed notifications (last 24h)", style = MaterialTheme.typography.titleMedium)
        if (unparsedCounts.isEmpty()) {
            Text("None.", color = InkFaded)
        } else {
            unparsedCounts.forEach { (provider, count) ->
                Text("$provider: $count", color = InkFaded)
            }
        }
    }
}

private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean =
    try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
