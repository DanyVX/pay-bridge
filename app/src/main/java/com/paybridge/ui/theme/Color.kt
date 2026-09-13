package com.paybridge.ui.theme

import androidx.compose.ui.graphics.Color

// Paper-ledger palette: warm off-white "paper" surfaces, ink-toned text, and stamp-ink accents
// per provider verdict. Deliberately avoids a dark-fintech-dashboard look.
val PaperBackground = Color(0xFFF4EEDD)
val PaperSurface = Color(0xFFFBF7EC)
val InkText = Color(0xFF2B2620)
val InkFaded = Color(0xFF6E6555)
val RuleLine = Color(0xFFD8CFB8)

val StampGreen = Color(0xFF2F6B3C)   // RECEIVED
val StampAmber = Color(0xFFB4791F)  // MISMATCH
val StampRed = Color(0xFF8A2E2E)    // TIMED OUT
val StampGrey = Color(0xFF5B5750)   // COULD NOT VERIFY / listener down
val StampBlue = Color(0xFF33566E)   // PENDING / waiting
