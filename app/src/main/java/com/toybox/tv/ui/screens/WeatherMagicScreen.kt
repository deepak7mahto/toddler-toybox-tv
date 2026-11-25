package com.toybox.tv.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private enum class WeatherKind(
    val title: String,
    val phrase: String,
    val top: Color,
    val bottom: Color,
    val sound: SoundEffect
) {
    SUNNY("Sunny Day", "Sunny! Bright sun!", Color(0xFF29B6F6), Color(0xFFB3E5FC), SoundEffect.SPARKLE),
    RAINY("Raindrops", "Rainy! Pitter patter!", Color(0xFF546E7A), Color(0xFF90A4AE), SoundEffect.WATER_SPLASH),
    SNOWY("Snowy Fun", "Snowy! Brrr! Snowman!", Color(0xFF5C6BC0), Color(0xFFC5CAE9), SoundEffect.CHIME),
    RAINBOW("Rainbow Sky", "Rainbow! So pretty!", Color(0xFF4FC3F7), Color(0xFFE1F5FE), SoundEffect.SPARKLE),
    STARRY("Starry Night", "Goodnight! Twinkle stars!", Color(0xFF1A237E), Color(0xFF3949AB), SoundEffect.CHIME)
}

@Composable
fun WeatherMagicScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val weathers = WeatherKind.entries
    var idx by remember { mutableIntStateOf(0) }
    val pulse = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val weather = weathers[idx]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(weather.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + weathers.size) % weathers.size
            ToyboxKey.RIGHT, ToyboxKey.OK -> idx = (idx + 1) % weathers.size
            ToyboxKey.UP, ToyboxKey.DOWN -> Unit
        }
        val active = weathers[idx]
        soundEngine.play(active.sound)
        voiceSpeaker.speak(active.phrase)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.45f,
            count = 32,
            baseColor = Color(0xFFFFEE58),
            shape = ParticleShape.STAR
        )
        launch {
            pulse.snapTo(1f)
            pulse.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                elapsed += dt
                particles.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(weather.top, weather.bottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f
            val r = minOf(size.width, size.height) * 0.22f * (1f + pulse.value * 0.14f)

            when (weather) {
                WeatherKind.SUNNY -> {
                    for (i in 0 until 12) {
                        val a = elapsed * 0.5f + i * (Math.PI.toFloat() / 6f)
                        drawLine(
                            color = Color(0xFFFFEE58),
                            start = Offset(cx + cos(a) * r * 1.1f, cy + sin(a) * r * 1.1f),
                            end = Offset(cx + cos(a) * r * 1.55f, cy + sin(a) * r * 1.55f),
                            strokeWidth = 18f
                        )
                    }
                    drawCircle(Color(0xFFFFD600), r, Offset(cx, cy))
                    drawCircle(Color(0xFF3E2723), r * 0.1f, Offset(cx - r * 0.28f, cy - r * 0.12f))
                    drawCircle(Color(0xFF3E2723), r * 0.1f, Offset(cx + r * 0.28f, cy - r * 0.12f))
                    drawArc(Color(0xFF3E2723), 20f, 140f, false, Offset(cx - r * 0.3f, cy + r * 0.05f), Size(r * 0.6f, r * 0.32f), style = Stroke(8f))
                }
                WeatherKind.RAINY -> {
                    // Falling raindrops
                    for (d in 0 until 24) {
                        val dx = ((d * 83 + 29) % 92 + 4) / 100f * size.width
                        val dy = ((elapsed * 0.65f + d * 0.17f) % 1f) * size.height
                        drawLine(Color(0xFF81D4FA), Offset(dx, dy), Offset(dx - 8f, dy + 34f), 8f)
                    }
                    // Big cheerful cloud
                    drawCircle(Color(0xFFCFD8DC), r * 0.85f, Offset(cx, cy - r * 0.3f))
                    drawCircle(Color(0xFFECEFF1), r * 0.72f, Offset(cx - r * 0.75f, cy - r * 0.15f))
                    drawCircle(Color(0xFFECEFF1), r * 0.72f, Offset(cx + r * 0.75f, cy - r * 0.15f))
                    // Colorful umbrella below
                    drawArc(Color(0xFFFF5252), 180f, 180f, true, Offset(cx - r * 0.9f, cy + r * 0.45f), Size(r * 1.8f, r * 1.1f))
                }
                WeatherKind.SNOWY -> {
                    for (f in 0 until 28) {
                        val fx = ((f * 97 + 13) % 96 + 2) / 100f * size.width + sin(elapsed * 2f + f) * 18f
                        val fy = ((elapsed * 0.28f + f * 0.19f) % 1f) * size.height
                        drawCircle(Color.White.copy(alpha = 0.85f), 8f + (f % 3) * 4f, Offset(fx, fy))
                    }
                    // Happy Snowman
                    drawCircle(Color.White, r * 0.88f, Offset(cx, cy + r * 0.62f))
                    drawCircle(Color.White, r * 0.62f, Offset(cx, cy - r * 0.35f))
                    drawCircle(Color(0xFF212121), r * 0.08f, Offset(cx - r * 0.2f, cy - r * 0.45f))
                    drawCircle(Color(0xFF212121), r * 0.08f, Offset(cx + r * 0.2f, cy - r * 0.45f))
                    drawCircle(Color(0xFFFF7043), r * 0.12f, Offset(cx, cy - r * 0.28f))
                }
                WeatherKind.RAINBOW -> {
                    val bands = listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFAB47BC))
                    bands.forEachIndexed { i, col ->
                        val rad = r * (2.2f - i * 0.24f)
                        drawArc(col, 180f, 180f, false, Offset(cx - rad, cy + r * 0.4f - rad), Size(rad * 2f, rad * 2f), style = Stroke(28f))
                    }
                    drawCircle(Color.White, r * 0.55f, Offset(cx - r * 1.7f, cy + r * 0.4f))
                    drawCircle(Color.White, r * 0.55f, Offset(cx + r * 1.7f, cy + r * 0.4f))
                }
                WeatherKind.STARRY -> {
                    for (s in 0 until 24) {
                        val sx = ((s * 173 + 31) % 94 + 3) / 100f * size.width
                        val sy = ((s * 89 + 17) % 72 + 5) / 100f * size.height
                        ParticleSystem.drawStar(this, Offset(sx, sy), 14f + (s % 3) * 8f, Color(0xFFFFEE58))
                    }
                    drawCircle(Color(0xFFFFF59D), r * 0.95f, Offset(cx, cy))
                    drawCircle(weather.top, r * 0.78f, Offset(cx + r * 0.38f, cy - r * 0.12f))
                }
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = weather.title.uppercase(),
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}
