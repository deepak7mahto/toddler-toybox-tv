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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlinx.coroutines.launch

private data class DrumPad(
    val key: ToyboxKey,
    val label: String,
    val sound: SoundEffect,
    val xFrac: Float,
    val yFrac: Float,
    val radiusFrac: Float,
    val bodyColor: Color,
    val rimColor: Color
)

@Composable
fun DrumBandScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val pads = remember {
        listOf(
            DrumPad(ToyboxKey.LEFT, "Snare", SoundEffect.DRUM_SNARE, 0.22f, 0.62f, 0.145f, Color(0xFFFF5252), Color(0xFFFFCDD2)),
            DrumPad(ToyboxKey.UP, "Cymbal", SoundEffect.DRUM_CYMBAL, 0.34f, 0.32f, 0.135f, Color(0xFFFFD600), Color(0xFFFFF59D)),
            DrumPad(ToyboxKey.OK, "Bass", SoundEffect.DRUM_KICK, 0.50f, 0.64f, 0.175f, Color(0xFF42A5F5), Color(0xFFBBDEFB)),
            DrumPad(ToyboxKey.DOWN, "Tom", SoundEffect.DRUM_TOM, 0.66f, 0.34f, 0.135f, Color(0xFF66BB6A), Color(0xFFC8E6C9)),
            DrumPad(ToyboxKey.RIGHT, "Bongo", SoundEffect.DRUM_TOM, 0.78f, 0.62f, 0.145f, Color(0xFFAB47BC), Color(0xFFE1BEE7))
        )
    }

    val hits = remember { List(pads.size) { Animatable(0f) } }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val idx = pads.indexOfFirst { it.key == lastKeyEvent.second }.coerceAtLeast(0)
        val pad = pads[idx]
        val rate = if (pad.key == ToyboxKey.RIGHT) 1.32f else 1.0f
        soundEngine.play(pad.sound, rate = rate)

        particles.burst(
            x = pad.xFrac * canvasWidth,
            y = pad.yFrac * canvasHeight,
            count = 32,
            baseColor = pad.bodyColor,
            speed = 480f,
            shape = ParticleShape.STAR
        )

        launch {
            hits[idx].snapTo(1f)
            hits[idx].animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
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
                    listOf(Color(0xFF4A148C), Color(0xFF6A1B9A), Color(0xFF8E24AA))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val baseUnit = minOf(size.width, size.height)
            pads.forEachIndexed { idx, pad ->
                val hit = hits[idx].value
                val cx = pad.xFrac * size.width
                val cy = pad.yFrac * size.height
                val r = baseUnit * pad.radiusFrac * (1f + hit * 0.16f)

                // Expanding shockwave ring on hit
                if (hit > 0.05f) {
                    drawCircle(
                        color = pad.rimColor.copy(alpha = hit * 0.7f),
                        radius = r * (1.15f + (1f - hit) * 0.55f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 12f)
                    )
                }

                // Drum rim & head
                drawCircle(pad.bodyColor, radius = r, center = Offset(cx, cy))
                drawCircle(pad.rimColor, radius = r * 0.80f, center = Offset(cx, cy))
                drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.28f, center = Offset(cx - r * 0.25f, cy - r * 0.25f))
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = "DRUM BAND!",
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFEE58),
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 10.dp)
        )
    }
}
