package com.paybridge.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paybridge.domain.model.Provider
import com.paybridge.ui.theme.PaperSurface
import com.paybridge.ui.theme.StampAmber

/**
 * Surfaces provider notification-format drift: if a provider changes their notification text,
 * every real payment from them silently starts failing to parse. Without this banner that
 * failure would have no visible symptom until a shopkeeper got burned by a false "not received".
 */
@Composable
fun UnparsedWarningBanner(unparsedCountsByProvider: Map<Provider, Int>) {
    if (unparsedCountsByProvider.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StampAmber)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        unparsedCountsByProvider.forEach { (provider, count) ->
            Text(
                "$count unparsed notification${if (count == 1) "" else "s"} from $provider in the last 24h — matching may be broken",
                color = PaperSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
