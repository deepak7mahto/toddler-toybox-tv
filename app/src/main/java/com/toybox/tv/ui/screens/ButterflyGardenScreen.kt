package com.toybox.tv.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Butterfly(
    var xFrac: Float,
    var yFrac: Float,
    var vx: Float,
    var vy: Float,
    val wingColor: Color,
    val phase: Float
)

@Composable
fun ButterflyGardenScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val wingColors = remember {
        listOf(
            Color(0xFFFF80AB),
            Color(0xFFFFCA28),
            Color(0xFF40C4FF),
            Color(0xFFAB47BC),
            Color(0xFFFF7043),
            Color(0xFF69F0AE)
        )
    }

    val butterflies = remember {
        mutableStateListOf<Butterfly>().apply {
            repeat(5) { i ->
                add(
                    Butterfly(
                        xFrac = 0.2f + i * 0.15f,
                        yFrac = 0.35f + (i % 2) * 0.18f,
                        vx = if (i % 2 == 0) 0.08f else -0.08f,
                        vy = -0.03f,
                        wingColor = wingColors[i % wingColors.size],
                        phase = i * 1.1f
                    )
                )
            }
        }
    }

    val particles = remember { ParticleSystem() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val flowerIdx = when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> 0
            ToyboxKey.UP -> 1
            ToyboxKey.OK -> 2
            ToyboxKey.DOWN -> 3
            ToyboxKey.RIGHT -> 4
        }
        val fx = 0.14f + flowerIdx * 0.18f
        val color = wingColors[Random.nextInt(wingColors.size)]
        if (butterflies.size >= 14) butterflies.removeAt(0)
        butterflies.add(
            Butterfly(
                xFrac = fx,
                yFrac = 0.68f,
                vx = (Random.nextFloat() - 0.5f) * 0.22f,
                vy = -0.10f - Random.nextFloat() * 0.06f,
                wingColor = color,
                phase = Random.nextFloat() * 6.28f
            )
        )
        soundEngine.play(SoundEffect.SPARKLE, rate = 0.9f + flowerIdx * 0.08f)
        particles.burst(
            x = fx * canvasWidth,
            y = canvasHeight * 0.68f,
            count = 24,
            baseColor = color,
            shape = ParticleShape.STAR
        )
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                elapsed += dt
                for (b in butterflies) {
                    b.xFrac += b.vx * dt
                    b.yFrac += (b.vy + sin(elapsed * 4f + b.phase) * 0.06f) * dt
                    if (b.xFrac < 0.06f || b.xFrac > 0.94f) b.vx = -b.vx
                    if (b.yFrac < 0.12f) b.vy = kotlin.math.abs(b.vy) * 0.6f
                    if (b.yFrac > 0.72f) b.vy = -kotlin.math.abs(b.vy) - 0.04f
                }
                particles.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFC8E6C9))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Meadow floor
            drawRect(Color(0xFF66BB6A), Offset(0f, size.height * 0.74f), Size(size.width, size.height * 0.26f))

            // 5 giant blooming flowers
            for (f in 0 until 5) {
                val fx = size.width * (0.14f + f * 0.18f)
                val fy = size.height * 0.68f
                val petalColor = wingColors[f % wingColors.size]
                drawLine(Color(0xFF2E7D32), Offset(fx, fy), Offset(fx, size.height * 0.88f), 14f)
                for (p in 0 until 6) {
                    val a = p * 60.0 * Math.PI / 180.0
                    drawCircle(petalColor, 34f, Offset(fx + (cos(a) * 40f).toFloat(), fy + (sin(a) * 40f).toFloat()))
                }
                drawCircle(Color(0xFFFFEE58), 28f, Offset(fx, fy))
            }

            // Fluttering butterflies
            for (b in butterflies) {
                val bx = b.xFrac * size.width
                val by = b.yFrac * size.height
                val flap = 0.55f + 0.45f * kotlin.math.abs(sin(elapsed * 9f + b.phase))
                drawOval(b.wingColor, Offset(bx - 52f * flap, by - 34f), Size(48f * flap, 58f))
                drawOval(b.wingColor, Offset(bx + 4f, by - 34f), Size(48f * flap, 58f))
                drawOval(Color(0xFF3E2723), Offset(bx - 6f, by - 24f), Size(12f, 48f))
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }
    }
}
