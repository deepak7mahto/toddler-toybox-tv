package com.toybox.tv.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import kotlinx.coroutines.launch

private data class XylophoneBar(
    val key: ToyboxKey,
    val note: SoundEffect,
    val color: Color,
    val glowColor: Color,
    val heightFraction: Float
)

@Composable
fun RainbowMusicScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val bars = remember {
        listOf(
            XylophoneBar(ToyboxKey.LEFT, SoundEffect.NOTE_C5, Color(0xFFFF5252), Color(0xFFFF8A80), 0.72f),
            XylophoneBar(ToyboxKey.UP, SoundEffect.NOTE_D5, Color(0xFFFF9800), Color(0xFFFFCC80), 0.66f),
            XylophoneBar(ToyboxKey.OK, SoundEffect.NOTE_E5, Color(0xFFFFD600), Color(0xFFFFFF8D), 0.60f),
            XylophoneBar(ToyboxKey.DOWN, SoundEffect.NOTE_G5, Color(0xFF66BB6A), Color(0xFFB9F6CA), 0.54f),
            XylophoneBar(ToyboxKey.RIGHT, SoundEffect.NOTE_A5, Color(0xFF42A5F5), Color(0xFF80D8FF), 0.48f)
        )
    }

    val barBounces = remember { List(bars.size) { Animatable(0f) } }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val idx = bars.indexOfFirst { it.key == lastKeyEvent.second }.coerceAtLeast(0)
        val bar = bars[idx]
        soundEngine.play(bar.note)

        val barCenterX = canvasWidth * (0.14f + idx * 0.18f)
        val barTopY = canvasHeight * (0.5f - bar.heightFraction * 0.35f)
        particles.burst(
            x = barCenterX,
            y = barTopY,
            count = 34,
            baseColor = bar.color,
            speed = 520f,
            shape = ParticleShape.STAR
        )

        launch {
            barBounces[idx].snapTo(1f)
            barBounces[idx].animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
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
                    listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Twinkling background stars
            for (s in 0 until 14) {
                val sx = ((s * 173 + 91) % 100) / 100f * size.width
                val sy = ((s * 97 + 43) % 45) / 100f * size.height
                drawCircle(Color.White.copy(alpha = 0.35f), radius = 4f + (s % 3) * 2f, center = Offset(sx, sy))
            }

            // Wooden xylophone rails
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(size.width * 0.06f, size.height * 0.38f),
                size = Size(size.width * 0.88f, 24f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(size.width * 0.06f, size.height * 0.64f),
                size = Size(size.width * 0.88f, 24f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 5 giant rainbow xylophone bars
            val barWidth = size.width * 0.135f
            bars.forEachIndexed { idx, bar ->
                val bounceVal = barBounces[idx].value
                val cx = size.width * (0.14f + idx * 0.18f)
                val barHeight = size.height * bar.heightFraction * (1f + bounceVal * 0.10f)
                val left = cx - barWidth / 2f
                val top = size.height * 0.52f - barHeight / 2f - bounceVal * 26f

                if (bounceVal > 0.05f) {
                    drawRoundRect(
                        color = bar.glowColor.copy(alpha = bounceVal * 0.55f),
                        topLeft = Offset(left - 14f, top - 14f),
                        size = Size(barWidth + 28f, barHeight + 28f),
                        cornerRadius = CornerRadius(38f, 38f)
                    )
                }

                drawRoundRect(
                    color = bar.color,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(30f, 30f)
                )

                // Gloss highlight + wooden pegs
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(left + 14f, top + 18f),
                    size = Size(barWidth * 0.22f, barHeight - 36f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawCircle(Color(0xFFFFF8E1), radius = 12f, center = Offset(cx, top + barHeight * 0.22f))
                drawCircle(Color(0xFFFFF8E1), radius = 12f, center = Offset(cx, top + barHeight * 0.78f))
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }
    }
}
