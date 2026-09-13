package com.paybridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Monospace throughout — the whole point of the design direction is to read like a
// thermal-printer receipt / paper ledger, not a generic sans-serif app UI.
private val ReceiptFont = FontFamily.Monospace

val PayBridgeTypography = Typography(
    headlineMedium = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = ReceiptFont, fontWeight = FontWeight.Normal, fontSize = 11.sp),
)
