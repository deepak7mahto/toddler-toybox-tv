package com.toybox.tv.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import kotlin.random.Random

private data class FireworkRocket(
    var xFraction: Float,
    var yFraction: Float,
    val targetY: Float,
    val color: Color
)

@Composable
fun FireworksSkyScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val fireworkColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFFD600),
            Color(0xFF40C4FF),
            Color(0xFF69F0AE),
            Color(0xFFFF80AB),
            Color(0xFFE040FB),
            Color(0xFFFFAB40)
        )
    }

    val rockets = remember { mutableStateListOf<FireworkRocket>() }
    val particles = remember { ParticleSystem(maxParticles = 320) }
    var launchCount by remember { mutableIntStateOf(0) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val xFrac = when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> 0.22f + Random.nextFloat() * 0.14f
            ToyboxKey.RIGHT -> 0.64f + Random.nextFloat() * 0.14f
            ToyboxKey.UP -> 0.35f + Random.nextFloat() * 0.30f
            ToyboxKey.DOWN -> 0.25f + Random.nextFloat() * 0.50f
            ToyboxKey.OK -> 0.42f + Random.nextFloat() * 0.16f
        }
        val color = fireworkColors[Random.nextInt(fireworkColors.size)]
        rockets.add(
            FireworkRocket(
                xFraction = xFrac,
                yFraction = 0.96f,
                targetY = 0.18f + Random.nextFloat() * 0.34f,
                color = color
            )
        )
        soundEngine.play(SoundEffect.PLOP, rate = 1.3f)
        launchCount++
        if (launchCount % 5 == 0) {
            voiceSpeaker.speak("Boom! Yay!")
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now

                val iter = rockets.iterator()
                while (iter.hasNext()) {
                    val r = iter.next()
                    r.yFraction -= 0.95f * dt
                    if (r.yFraction <= r.targetY) {
                        val bx = r.xFraction * canvasWidth
                        val by = r.yFraction * canvasHeight
                        particles.burst(
                            x = bx,
                            y = by,
                            count = 48,
                            baseColor = r.color,
                            speed = 620f,
                            shape = ParticleShape.STAR
                        )
                        particles.burst(
                            x = bx,
                            y = by,
                            count = 24,
                            baseColor = Color(0xFFFFEE58),
                            speed = 340f,
                            shape = ParticleShape.CIRCLE
                        )
                        soundEngine.play(SoundEffect.FIREWORK_BOOM, rate = 0.85f + Random.nextFloat() * 0.3f)
                        iter.remove()
                    }
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
                    listOf(Color(0xFF0D1B3E), Color(0xFF1A237E), Color(0xFF283593))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Twinkling stars
            for (i in 0 until 28) {
                val sx = ((i * 197 + 41) % 100) / 100f * size.width
                val sy = ((i * 83 + 19) % 68) / 100f * size.height
                drawCircle(Color.White.copy(alpha = 0.55f), radius = 3f + (i % 3) * 2f, center = Offset(sx, sy))
            }

            // Smiling crescent moon
            val moonCenter = Offset(size.width * 0.86f, size.height * 0.17f)
            val moonR = size.height * 0.09f
            drawCircle(Color(0xFFFFF59D), radius = moonR, center = moonCenter)
            drawCircle(Color(0xFF131F54), radius = moonR * 0.82f, center = Offset(moonCenter.x + moonR * 0.35f, moonCenter.y - moonR * 0.12f))

            // Dark silhouette hills
            val hill = Path().apply {
                moveTo(0f, size.height * 0.86f)
                quadraticTo(size.width * 0.25f, size.height * 0.78f, size.width * 0.5f, size.height * 0.86f)
                quadraticTo(size.width * 0.75f, size.height * 0.93f, size.width, size.height * 0.83f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(hill, Color(0xFF0A122A))

            // Rising rockets
            for (r in rockets) {
                val rx = r.xFraction * size.width
                val ry = r.yFraction * size.height
                drawLine(
                    color = Color(0xFFFFEE58),
                    start = Offset(rx, ry),
                    end = Offset(rx, ry + 56f),
                    strokeWidth = 10f
                )
                drawCircle(r.color, radius = 14f, center = Offset(rx, ry))
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }
    }
}
