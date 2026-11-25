package com.toybox.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ToyboxColors {
    val SkyTop = Color(0xFF29B6F6)
    val SkyBottom = Color(0xFF81D4FA)
    val Meadow = Color(0xFF66BB6A)
    val SunYellow = Color(0xFFFFEB3B)
    val FocusGold = Color(0xFFFFD600)
    val Coral = Color(0xFFFF5252)
    val Tangerine = Color(0xFFFF9800)
    val Mint = Color(0xFF26A69A)
    val Grape = Color(0xFFAB47BC)
    val RoyalBlue = Color(0xFF42A5F5)
    val Cream = Color(0xFFFFFDE7)
    val DeepNavy = Color(0xFF1A237E)
}

object TvOverscan {
    val Horizontal = 48.dp
    val Vertical = 27.dp
}

private val ToyboxColorScheme = lightColorScheme(
    primary = ToyboxColors.Coral,
    secondary = ToyboxColors.SunYellow,
    tertiary = ToyboxColors.Mint,
    background = ToyboxColors.SkyTop,
    surface = ToyboxColors.Cream,
    onPrimary = Color.White,
    onBackground = ToyboxColors.DeepNavy,
    onSurface = ToyboxColors.DeepNavy
)

@Composable
fun ToddlerToyboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ToyboxColorScheme,
        content = content
    )
}
