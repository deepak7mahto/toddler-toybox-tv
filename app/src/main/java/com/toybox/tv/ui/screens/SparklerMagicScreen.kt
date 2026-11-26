package com.toybox.tv.ui.screens

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class TrailDot(
    val x: Float,
    val y: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val radius: Float
)

private enum class SparklerPattern(val title: String) {
    HEART("Magic Heart"),
    STAR("Golden Star"),
    SPIRAL("Rainbow Spiral"),
    BUTTERFLY("Sparkle Loops"),
    RAINBOW("Sky Rainbow")
}

@Composable
fun SparklerMagicScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val patterns = SparklerPattern.entries
    val glowColors = remember {
        listOf(
            Color(0xFFFFD600),
            Color(0xFFFF80AB),
            Color(0xFF40C4FF),
            Color(0xFF69F0AE),
            Color(0xFFFFAB40),
            Color(0xFFE040FB)
        )
    }

    var patternIdx by remember { mutableIntStateOf(0) }
    var colorIdx by remember { mutableIntStateOf(0) }
    var phase by remember { mutableFloatStateOf(0f) }
    var speedBoost by remember { mutableFloatStateOf(1f) }
    val trail = remember { mutableStateListOf<TrailDot>() }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var tipX by remember { mutableFloatStateOf(960f) }
    var tipY by remember { mutableFloatStateOf(540f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val pattern = patterns[patternIdx]

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> patternIdx = (patternIdx - 1 + patterns.size) % patterns.size
            ToyboxKey.RIGHT -> patternIdx = (patternIdx + 1) % patterns.size
            else -> {
                colorIdx = (colorIdx + 1) % glowColors.size
                if (lastKeyEvent.second == ToyboxKey.OK) {
                    patternIdx = (patternIdx + 1) % patterns.size
                }
            }
        }
        speedBoost = 2.6f
        soundEngine.play(SoundEffect.SPARKLER_CRACKLE, rate = 0.88f + (colorIdx % 4) * 0.1f)
        particles.burst(
            x = tipX,
            y = tipY,
            count = 32,
            baseColor = glowColors[colorIdx],
            speed = 480f,
            shape = ParticleShape.STAR
        )
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                phase += dt * 1.9f * speedBoost
                speedBoost = (speedBoost - dt * 1.4f).coerceAtLeast(1f)

                val cx = canvasWidth * 0.5f
                val cy = canvasHeight * 0.52f
                val rx = canvasWidth * 0.28f
                val ry = canvasHeight * 0.28f

                val (nx, ny) = when (patterns[patternIdx]) {
                    SparklerPattern.HEART -> {
                        val t = phase
                        val hx = 16f * sin(t) * sin(t) * sin(t)
                        val hy = -(13f * cos(t) - 5f * cos(2f * t) - 2f * cos(3f * t) - cos(4f * t))
                        (cx + hx * (rx / 18f)) to (cy + hy * (ry / 18f))
                    }
                    SparklerPattern.STAR -> {
                        val t = phase
                        val rad = 0.55f + 0.45f * cos(5f * t)
                        (cx + cos(t) * rx * rad) to (cy + sin(t) * ry * rad)
                    }
                    SparklerPattern.SPIRAL -> {
                        val t = phase * 1.5f
                        val rad = 0.25f + 0.75f * kotlin.math.abs(sin(t * 0.35f))
                        (cx + cos(t) * rx * rad) to (cy + sin(t) * ry * rad)
                    }
                    SparklerPattern.BUTTERFLY -> {
                        val t = phase
                        (cx + sin(t) * rx * 1.1f) to (cy + sin(2f * t) * ry * 0.85f)
                    }
                    SparklerPattern.RAINBOW -> {
                        val t = (sin(phase * 0.8f) * 0.5f + 0.5f) * PI.toFloat()
                        (cx - cos(t) * rx * 1.25f) to (cy + ry * 0.55f - sin(t) * ry * 1.2f)
                    }
                }
                tipX = nx
                tipY = ny

                if (trail.size < 180) {
                    val col = glowColors[(colorIdx + (phase.toInt() % glowColors.size)) % glowColors.size]
                    trail.add(TrailDot(nx, ny, 1.35f, 1.35f, col, 14f + Random.nextFloat() * 8f))
                }

                val iter = trail.iterator()
                while (iter.hasNext()) {
                    val d = iter.next()
                    d.life -= dt
                    if (d.life <= 0f) iter.remove()
                }

                particles.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070B24), Color(0xFF141842), Color(0xFF1E1B4B))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Shimmering neon sparkler trail
            for (d in trail) {
                val a = (d.life / d.maxLife).coerceIn(0f, 1f)
                drawCircle(d.color.copy(alpha = a * 0.4f), d.radius * 1.7f * a, Offset(d.x, d.y))
                drawCircle(d.color.copy(alpha = a), d.radius * a, Offset(d.x, d.y))
            }

            // Phuljhari sparkler stick + blazing white-gold tip
            drawLine(
                color = Color(0xFFCFD8DC),
                start = Offset(tipX, tipY),
                end = Offset(tipX + 90f, tipY + 115f),
                strokeWidth = 9f
            )
            ParticleSystem.drawStar(this, Offset(tipX, tipY), 38f, Color(0xFFFFEE58))
            drawCircle(Color.White, 16f, Offset(tipX, tipY))

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = "SPARKLER • ${pattern.title.uppercase()}",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFEE58),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 10.dp)
        )
    }
}
