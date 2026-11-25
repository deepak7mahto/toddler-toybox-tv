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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import kotlin.random.Random

private data class PaintBlob(
    val xFrac: Float,
    val yFrac: Float,
    val radius: Float,
    val color: Color,
    val isStar: Boolean
)

@Composable
fun PaintSplashScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val palette = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFF9800),
            Color(0xFFFFD600),
            Color(0xFF66BB6A),
            Color(0xFF29B6F6),
            Color(0xFFAB47BC),
            Color(0xFFFF80AB),
            Color(0xFF26A69A)
        )
    }

    val blobs = remember { mutableStateListOf<PaintBlob>() }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val key = lastKeyEvent.second
        // DOWN clears the canvas when full so kids can paint fresh!
        if (key == ToyboxKey.DOWN && blobs.size >= 14) {
            blobs.clear()
            soundEngine.play(SoundEffect.SPARKLE)
            return@LaunchedEffect
        }

        val xFrac = when (key) {
            ToyboxKey.LEFT -> 0.12f + Random.nextFloat() * 0.28f
            ToyboxKey.RIGHT -> 0.60f + Random.nextFloat() * 0.28f
            ToyboxKey.UP -> 0.20f + Random.nextFloat() * 0.60f
            else -> 0.14f + Random.nextFloat() * 0.72f
        }
        val yFrac = when (key) {
            ToyboxKey.UP -> 0.14f + Random.nextFloat() * 0.30f
            ToyboxKey.DOWN -> 0.56f + Random.nextFloat() * 0.30f
            else -> 0.18f + Random.nextFloat() * 0.64f
        }

        val color = palette[Random.nextInt(palette.size)]
        val radius = 58f + Random.nextFloat() * 68f
        if (blobs.size >= 42) blobs.removeAt(0)
        blobs.add(PaintBlob(xFrac, yFrac, radius, color, isStar = Random.nextFloat() < 0.28f))

        soundEngine.play(SoundEffect.SPLAT, rate = 0.85f + Random.nextFloat() * 0.4f)
        particles.burst(
            x = xFrac * canvasWidth,
            y = yFrac * canvasHeight,
            count = 26,
            baseColor = color,
            shape = ParticleShape.CIRCLE
        )
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
            .background(Color(0xFFFFFDE7))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            for (b in blobs) {
                val cx = b.xFrac * size.width
                val cy = b.yFrac * size.height
                if (b.isStar) {
                    ParticleSystem.drawStar(this, Offset(cx, cy), b.radius * 1.15f, b.color)
                } else {
                    drawCircle(b.color, b.radius, Offset(cx, cy))
                    // Little splatter droplets around each main paint blob
                    drawCircle(b.color, b.radius * 0.32f, Offset(cx - b.radius * 0.95f, cy - b.radius * 0.45f))
                    drawCircle(b.color, b.radius * 0.26f, Offset(cx + b.radius * 0.92f, cy + b.radius * 0.48f))
                    drawCircle(b.color, b.radius * 0.22f, Offset(cx + b.radius * 0.55f, cy - b.radius * 0.85f))
                }
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        if (blobs.isEmpty()) {
            Text(
                text = "PRESS ANY BUTTON TO SPLASH PAINT!",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFAB47BC),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = TvOverscan.Horizontal)
            )
        }
    }
}
