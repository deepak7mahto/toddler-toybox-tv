package com.toybox.tv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.ToyboxColors
import com.toybox.tv.ui.theme.TvOverscan

enum class ToyboxMode(val title: String, val accent: Color, val cardBg: Color) {
    BALLOON_POP("Balloons", Color(0xFFFF5252), Color(0xFFFFEBEE)),
    ANIMAL_FRIENDS("Animals", Color(0xFFFF9800), Color(0xFFFFF8E1)),
    RAINBOW_MUSIC("Xylophone", Color(0xFFAB47BC), Color(0xFFF3E5F5)),
    HONK_AND_GO("Honk & Go", Color(0xFF26A69A), Color(0xFFE0F2F1)),
    NURSERY_PIANO("Songs", Color(0xFF5E35B1), Color(0xFFEDE7F6)),
    FIREWORKS_SKY("Fireworks", Color(0xFF1E88E5), Color(0xFFE3F2FD)),
    PEEK_A_BOO("Peek-a-Boo", Color(0xFFE53935), Color(0xFFFFF3E0)),
    COLORS_SHAPES("Shapes", Color(0xFF43A047), Color(0xFFE8F5E9)),
    ABC_A_TO_Z("A to Z", Color(0xFF1565C0), Color(0xFFE3F2FD)),
    NUMBERS_100("1 to 100", Color(0xFF00897B), Color(0xFFE0F2F1)),
    DRUM_BAND("Drums", Color(0xFF8E24AA), Color(0xFFF3E5F5)),
    OCEAN_FRIENDS("Ocean", Color(0xFF0277BD), Color(0xFFE1F5FE)),
    WEATHER_MAGIC("Weather", Color(0xFF0288D1), Color(0xFFE1F5FE)),
    DINOSAUR_STOMP("Dinos", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    YUMMY_FOOD("Yummy", Color(0xFFE65100), Color(0xFFFFF3E0)),
    PAINT_SPLASH("Paint", Color(0xFFD81B60), Color(0xFFFCE4EC)),
    SPACE_EXPLORER("Space", Color(0xFF283593), Color(0xFFE8EAF6)),
    BUTTERFLY_GARDEN("Garden", Color(0xFF7B1FA2), Color(0xFFF3E5F5))
}

private val TileShape = RoundedCornerShape(20.dp)

@Composable
fun ToyboxHomeScreen(
    selectedIndex: Int,
    showExitHint: Boolean,
    onSelectMode: (ToyboxMode) -> Unit,
    onStartDemo: () -> Unit
) {
    val modes = ToyboxMode.entries
    val demoSelected = selectedIndex == -1
    val skyBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF29B6F6), Color(0xFF81D4FA), Color(0xFFB3E5FC))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skyBrush)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val hill = Path().apply {
                    moveTo(0f, h * 0.87f)
                    quadraticTo(w * 0.25f, h * 0.79f, w * 0.5f, h * 0.87f)
                    quadraticTo(w * 0.75f, h * 0.94f, w, h * 0.85f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                onDrawBehind {
                    drawCircle(Color(0xFFFFEE58), radius = h * 0.09f, center = Offset(w * 0.08f, h * 0.12f))
                    drawCloud(Offset(w * 0.28f, h * 0.11f), h * 0.045f)
                    drawCloud(Offset(w * 0.84f, h * 0.12f), h * 0.052f)
                    drawPath(hill, Color(0xFF66BB6A))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TvOverscan.Horizontal, vertical = TvOverscan.Vertical),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODDLER TOYBOX (18)",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                val demoScale by animateFloatAsState(
                    targetValue = if (demoSelected) 1.07f else 1.0f,
                    animationSpec = tween(durationMillis = 120),
                    label = "demoScale"
                )
                val pillShape = RoundedCornerShape(28.dp)
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = demoScale
                            scaleY = demoScale
                        }
                        .clip(pillShape)
                        .background(if (demoSelected) Color(0xFFFF7043) else Color(0xFFFF9800))
                        .border(
                            width = if (demoSelected) 5.dp else 2.5.dp,
                            color = if (demoSelected) ToyboxColors.FocusGold else Color.White,
                            shape = pillShape
                        )
                        .clickable { onStartDemo() }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "▶ SURPRISE SHOW (10s DEMO)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6 columns x 3 rows = 18 games
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0 until 6) {
                            val idx = row * 6 + col
                            val mode = modes[idx]
                            ModeTile(
                                mode = mode,
                                selected = selectedIndex == idx,
                                onClick = { onSelectMode(mode) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (showExitHint) {
                    "Press BACK again to exit"
                } else {
                    "Pick any toy with OK  •  Press BACK inside a game to switch"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (showExitHint) ToyboxColors.SunYellow else Color.White
            )
        }
    }
}

@Composable
private fun ModeTile(
    mode: ToyboxMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "tileScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(TileShape)
            .background(mode.cardBg)
            .border(
                width = if (selected) 6.dp else 2.dp,
                color = if (selected) ToyboxColors.FocusGold else Color.White,
                shape = TileShape
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .drawWithCache {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = minOf(size.width, size.height) * 0.36f
                        val cachedPath = buildModePath(mode, Offset(cx, cy), r)
                        onDrawBehind {
                            drawModeIcon(mode, Offset(cx, cy), r, cachedPath)
                        }
                    }
            )

            Text(
                text = mode.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = mode.accent,
                maxLines = 1
            )
        }
    }
}

private fun buildModePath(mode: ToyboxMode, c: Offset, r: Float): Path? {
    val cx = c.x
    val cy = c.y
    return when (mode) {
        ToyboxMode.ABC_A_TO_Z -> Path().apply {
            moveTo(cx, cy - r * 0.58f)
            lineTo(cx - r * 0.46f, cy + r * 0.55f)
            moveTo(cx, cy - r * 0.58f)
            lineTo(cx + r * 0.46f, cy + r * 0.55f)
            moveTo(cx - r * 0.28f, cy + r * 0.15f)
            lineTo(cx + r * 0.28f, cy + r * 0.15f)
        }
        ToyboxMode.OCEAN_FRIENDS -> Path().apply {
            moveTo(cx - r * 0.55f, cy)
            lineTo(cx - r * 1.15f, cy - r * 0.42f)
            lineTo(cx - r * 1.15f, cy + r * 0.42f)
            close()
        }
        else -> null
    }
}

private fun DrawScope.drawModeIcon(mode: ToyboxMode, c: Offset, r: Float, cachedPath: Path?) {
    val cx = c.x
    val cy = c.y
    when (mode) {
        ToyboxMode.BALLOON_POP -> {
            drawOval(Color(0xFFFF5252), Offset(cx - r * 1.1f, cy - r * 0.7f), Size(r * 0.9f, r * 1.15f))
            drawOval(Color(0xFFFFCA28), Offset(cx - r * 0.45f, cy - r * 0.95f), Size(r * 0.95f, r * 1.2f))
            drawOval(Color(0xFF40C4FF), Offset(cx + r * 0.25f, cy - r * 0.65f), Size(r * 0.9f, r * 1.15f))
        }
        ToyboxMode.ANIMAL_FRIENDS -> {
            drawCircle(Color(0xFFFF7043), radius = r * 1.05f, center = c)
            drawCircle(Color(0xFFFFCA28), radius = r * 0.76f, center = c)
            drawCircle(Color(0xFF3E2723), radius = r * 0.11f, center = Offset(cx - r * 0.28f, cy - r * 0.14f))
            drawCircle(Color(0xFF3E2723), radius = r * 0.11f, center = Offset(cx + r * 0.28f, cy - r * 0.14f))
            drawCircle(Color(0xFF5D4037), radius = r * 0.12f, center = Offset(cx, cy + r * 0.08f))
        }
        ToyboxMode.RAINBOW_MUSIC -> {
            val cols = listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFD600), Color(0xFF66BB6A), Color(0xFF42A5F5))
            val bw = r * 0.4f
            val gap = r * 0.1f
            val sx = cx - (cols.size * bw + (cols.size - 1) * gap) / 2f
            cols.forEachIndexed { idx, col ->
                val bh = r * (1.65f - idx * 0.16f)
                drawRoundRect(col, Offset(sx + idx * (bw + gap), cy - bh / 2f), Size(bw, bh), CornerRadius(10f, 10f))
            }
        }
        ToyboxMode.HONK_AND_GO -> {
            drawRoundRect(Color(0xFFEF5350), Offset(cx - r * 1.2f, cy - r * 0.55f), Size(r * 2.4f, r * 1.0f), CornerRadius(18f, 18f))
            drawRoundRect(Color(0xFFB3E5FC), Offset(cx + r * 0.32f, cy - r * 0.38f), Size(r * 0.65f, r * 0.42f), CornerRadius(8f, 8f))
            drawCircle(Color(0xFF263238), r * 0.32f, Offset(cx - r * 0.62f, cy + r * 0.48f))
            drawCircle(Color(0xFF263238), r * 0.32f, Offset(cx + r * 0.62f, cy + r * 0.48f))
        }
        ToyboxMode.NURSERY_PIANO -> {
            val cols = listOf(Color(0xFFFF5252), Color(0xFFFFD600), Color(0xFF66BB6A), Color(0xFF42A5F5))
            val bw = r * 0.48f
            val sx = cx - (cols.size * bw) / 2f
            cols.forEachIndexed { i, col ->
                drawRoundRect(col, Offset(sx + i * bw + 2f, cy - r * 0.15f), Size(bw - 4f, r * 1.0f), CornerRadius(8f, 8f))
            }
            ParticleSystem.drawStar(this, Offset(cx, cy - r * 0.62f), r * 0.42f, Color(0xFFFFD600))
        }
        ToyboxMode.FIREWORKS_SKY -> {
            drawCircle(Color(0xFF1A237E), r * 1.05f, c)
            ParticleSystem.drawStar(this, Offset(cx - r * 0.25f, cy - r * 0.18f), r * 0.48f, Color(0xFFFFD600))
            ParticleSystem.drawStar(this, Offset(cx + r * 0.38f, cy + r * 0.22f), r * 0.35f, Color(0xFFFF5252))
            drawCircle(Color(0xFF40C4FF), r * 0.14f, Offset(cx + r * 0.35f, cy - r * 0.42f))
        }
        ToyboxMode.PEEK_A_BOO -> {
            drawOval(Color.White, Offset(cx - r * 0.48f, cy - r * 1.05f), Size(r * 0.32f, r * 0.72f))
            drawOval(Color.White, Offset(cx + r * 0.16f, cy - r * 1.05f), Size(r * 0.32f, r * 0.72f))
            drawRoundRect(Color(0xFFFF5252), Offset(cx - r * 0.85f, cy - r * 0.25f), Size(r * 1.7f, r * 1.15f), CornerRadius(12f, 12f))
            drawRect(Color(0xFFFFD600), Offset(cx - r * 0.16f, cy - r * 0.25f), Size(r * 0.32f, r * 1.15f))
        }
        ToyboxMode.COLORS_SHAPES -> {
            drawCircle(Color(0xFFFF5252), r * 0.55f, Offset(cx - r * 0.55f, cy + r * 0.22f))
            drawRoundRect(Color(0xFF42A5F5), Offset(cx + r * 0.05f, cy - r * 0.15f), Size(r * 0.9f, r * 0.9f), CornerRadius(10f, 10f))
            ParticleSystem.drawStar(this, Offset(cx, cy - r * 0.48f), r * 0.48f, Color(0xFFFFD600))
        }
        ToyboxMode.ABC_A_TO_Z -> {
            drawRoundRect(Color(0xFF42A5F5), Offset(cx - r * 0.95f, cy - r * 0.95f), Size(r * 1.9f, r * 1.9f), CornerRadius(18f, 18f))
            cachedPath?.let { drawPath(it, Color.White, style = Stroke(width = r * 0.22f)) }
        }
        ToyboxMode.NUMBERS_100 -> {
            drawRoundRect(Color(0xFFFF5252), Offset(cx - r * 1.1f, cy + r * 0.05f), Size(r * 0.65f, r * 0.85f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFFFFCA28), Offset(cx - r * 0.32f, cy - r * 0.30f), Size(r * 0.65f, r * 1.2f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF26A69A), Offset(cx + r * 0.46f, cy - r * 0.68f), Size(r * 0.65f, r * 1.58f), CornerRadius(8f, 8f))
            ParticleSystem.drawStar(this, Offset(cx - r * 0.72f, cy - r * 0.52f), r * 0.34f, Color(0xFFFFD600))
        }
        ToyboxMode.DRUM_BAND -> {
            drawCircle(Color(0xFF42A5F5), r * 0.68f, Offset(cx, cy + r * 0.18f))
            drawCircle(Color(0xFFBBDEFB), r * 0.50f, Offset(cx, cy + r * 0.18f))
            drawCircle(Color(0xFFFF5252), r * 0.45f, Offset(cx - r * 0.72f, cy - r * 0.32f))
            drawCircle(Color(0xFFFFD600), r * 0.45f, Offset(cx + r * 0.72f, cy - r * 0.32f))
        }
        ToyboxMode.OCEAN_FRIENDS -> {
            cachedPath?.let { drawPath(it, Color(0xFF0288D1)) }
            drawOval(Color(0xFF29B6F6), Offset(cx - r * 0.65f, cy - r * 0.55f), Size(r * 1.65f, r * 1.1f))
            drawCircle(Color.White, r * 0.14f, Offset(cx + r * 0.48f, cy - r * 0.12f))
            drawCircle(Color(0xFF212121), r * 0.08f, Offset(cx + r * 0.48f, cy - r * 0.12f))
        }
        ToyboxMode.WEATHER_MAGIC -> {
            drawCircle(Color(0xFFFFD600), r * 0.65f, Offset(cx - r * 0.32f, cy - r * 0.25f))
            drawCircle(Color.White, r * 0.55f, Offset(cx + r * 0.22f, cy + r * 0.22f))
            drawCircle(Color.White, r * 0.42f, Offset(cx - r * 0.25f, cy + r * 0.32f))
            drawCircle(Color.White, r * 0.42f, Offset(cx + r * 0.65f, cy + r * 0.32f))
        }
        ToyboxMode.DINOSAUR_STOMP -> {
            drawOval(Color(0xFF66BB6A), Offset(cx - r * 0.88f, cy - r * 0.15f), Size(r * 1.45f, r * 0.85f))
            drawCircle(Color(0xFF66BB6A), r * 0.48f, Offset(cx + r * 0.55f, cy - r * 0.45f))
            drawCircle(Color(0xFFFFCA28), r * 0.18f, Offset(cx - r * 0.35f, cy - r * 0.25f))
            drawCircle(Color(0xFFFFCA28), r * 0.18f, Offset(cx + r * 0.05f, cy - r * 0.28f))
            drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx + r * 0.68f, cy - r * 0.52f))
        }
        ToyboxMode.YUMMY_FOOD -> {
            drawCircle(Color(0xFFFF5252), r * 0.82f, Offset(cx, cy + r * 0.12f))
            drawOval(Color(0xFF66BB6A), Offset(cx + r * 0.05f, cy - r * 0.98f), Size(r * 0.52f, r * 0.35f))
            drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx - r * 0.22f, cy + r * 0.05f))
            drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx + r * 0.22f, cy + r * 0.05f))
        }
        ToyboxMode.PAINT_SPLASH -> {
            drawCircle(Color(0xFFFF5252), r * 0.55f, Offset(cx - r * 0.42f, cy - r * 0.22f))
            drawCircle(Color(0xFFFFD600), r * 0.52f, Offset(cx + r * 0.42f, cy - r * 0.18f))
            drawCircle(Color(0xFF29B6F6), r * 0.55f, Offset(cx, cy + r * 0.38f))
        }
        ToyboxMode.SPACE_EXPLORER -> {
            drawCircle(Color(0xFF1A237E), r * 1.05f, c)
            drawOval(Color(0xFFFF7043), Offset(cx - r * 0.95f, cy - r * 0.22f), Size(r * 1.9f, r * 0.44f), style = Stroke(8f))
            drawCircle(Color(0xFFFFCA28), r * 0.52f, c)
        }
        ToyboxMode.BUTTERFLY_GARDEN -> {
            drawOval(Color(0xFFFF80AB), Offset(cx - r * 0.92f, cy - r * 0.62f), Size(r * 0.85f, r * 1.15f))
            drawOval(Color(0xFFFF80AB), Offset(cx + r * 0.07f, cy - r * 0.62f), Size(r * 0.85f, r * 1.15f))
            drawOval(Color(0xFF3E2723), Offset(cx - r * 0.12f, cy - r * 0.45f), Size(r * 0.24f, r * 0.95f))
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, r: Float) {
    val white = Color.White.copy(alpha = 0.85f)
    drawCircle(white, radius = r, center = center)
    drawCircle(white, radius = r * 0.8f, center = Offset(center.x - r * 0.85f, center.y + r * 0.15f))
    drawCircle(white, radius = r * 0.85f, center = Offset(center.x + r * 0.9f, center.y + r * 0.12f))
}
