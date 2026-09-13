package com.paybridge.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The rotated, ink-bordered "rubber stamp" visual used for every claim verdict — the central
 * piece of the paper-ledger design direction. A slight rotation and a thick colored border
 * read as a physical stamp mark rather than a rounded status chip.
 */
@Composable
fun StampBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .rotate(-6f)
            .border(BorderStroke(3.dp, color))
            .padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
