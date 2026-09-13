package com.paybridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light-only, on purpose: a paper ledger doesn't have a "dark mode," and the shopkeeper's
// counter is typically lit — a forced dark scheme would fight the design direction.
private val PayBridgeColorScheme = lightColorScheme(
    primary = InkText,
    onPrimary = PaperSurface,
    secondary = StampBlue,
    background = PaperBackground,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText,
    surfaceVariant = PaperBackground,
    onSurfaceVariant = InkFaded,
    outline = RuleLine,
    error = StampRed,
)

@Composable
fun PayBridgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PayBridgeColorScheme,
        typography = PayBridgeTypography,
        shapes = PayBridgeShapes,
        content = content,
    )
}
