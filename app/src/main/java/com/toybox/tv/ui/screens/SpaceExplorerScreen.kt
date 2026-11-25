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
import kotlin.math.sin

private enum class SpaceWonder(
    val displayName: String,
    val phrase: String,
    val mainColor: Color,
    val accentColor: Color
) {
    EARTH("Planet Earth", "Earth! Our home!", Color(0xFF29B6F6), Color(0xFF66BB6A)),
    MOON("Smiling Moon", "Moon! Cheese moon!", Color(0xFFFFF59D), Color(0xFFFFE082)),
    SATURN("Saturn", "Saturn! Big rings!", Color(0xFFFFCA28), Color(0xFFFF7043)),
    MARS("Red Mars", "Mars! Red planet!", Color(0xFFEF5350), Color(0xFFFF8A65)),
    ALIEN("Friendly Alien", "Beep boop! Friendly Alien!", Color(0xFF66BB6A), Color(0xFFAB47BC)),
    SUN("Big Sun", "The Sun! Bright and warm!", Color(0xFFFFD600), Color(0xFFFF9800))
}

@Composable
fun SpaceExplorerScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val wonders = SpaceWonder.entries
    var idx by remember { mutableIntStateOf(0) }
    val zoom = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val wonder = wonders[idx]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(wonder.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + wonders.size) % wonders.size
            ToyboxKey.RIGHT, ToyboxKey.OK -> idx = (idx + 1) % wonders.size
            ToyboxKey.UP, ToyboxKey.DOWN -> Unit
        }
        val active = wonders[idx]
        soundEngine.play(if (active == SpaceWonder.ALIEN) SoundEffect.ANIMAL_BOING else SoundEffect.SPARKLE)
        voiceSpeaker.speak(active.phrase)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.48f,
            count = 34,
            baseColor = active.mainColor,
            shape = ParticleShape.STAR
        )
        launch {
            zoom.snapTo(1f)
            zoom.animateTo(0f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
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
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E2A), Color(0xFF1A237E), Color(0xFF283593))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Drifting starfield
            for (s in 0 until 32) {
                val sx = ((1f - ((elapsed * 0.06f + s * 0.13f) % 1f)) * size.width)
                val sy = ((s * 97 + 23) % 92 + 4) / 100f * size.height
                drawCircle(Color.White.copy(alpha = 0.7f), 3f + (s % 3) * 2f, Offset(sx, sy))
            }

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f + sin(elapsed * 2f) * 18f
            val r = minOf(size.width, size.height) * 0.23f * (1f + zoom.value * 0.15f)

            when (wonder) {
                SpaceWonder.SATURN -> {
                    drawOval(wonder.accentColor, Offset(cx - r * 1.85f, cy - r * 0.35f), Size(r * 3.7f, r * 0.7f), style = Stroke(24f))
                    drawCircle(wonder.mainColor, r, Offset(cx, cy))
                }
                SpaceWonder.EARTH -> {
                    drawCircle(wonder.mainColor, r, Offset(cx, cy))
                    drawCircle(wonder.accentColor, r * 0.38f, Offset(cx - r * 0.32f, cy - r * 0.22f))
                    drawCircle(wonder.accentColor, r * 0.32f, Offset(cx + r * 0.36f, cy + r * 0.24f))
                }
                SpaceWonder.ALIEN -> {
                    // Cute UFO & friendly green alien
                    drawOval(wonder.accentColor, Offset(cx - r * 1.4f, cy + r * 0.1f), Size(r * 2.8f, r * 0.75f))
                    drawCircle(wonder.mainColor, r * 0.75f, Offset(cx, cy - r * 0.15f))
                    // 3 cute alien eyes!
                    for (e in -1..1) {
                        drawCircle(Color.White, r * 0.16f, Offset(cx + e * r * 0.32f, cy - r * 0.25f))
                        drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx + e * r * 0.32f, cy - r * 0.25f))
                    }
                }
                else -> {
                    drawCircle(wonder.mainColor, r, Offset(cx, cy))
                    drawCircle(wonder.accentColor, r * 0.22f, Offset(cx - r * 0.35f, cy - r * 0.25f))
                    drawCircle(wonder.accentColor, r * 0.16f, Offset(cx + r * 0.32f, cy + r * 0.22f))
                }
            }

            if (wonder != SpaceWonder.ALIEN) {
                drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx - r * 0.26f, cy - r * 0.08f))
                drawCircle(Color(0xFF212121), r * 0.09f, Offset(cx + r * 0.26f, cy - r * 0.08f))
                drawArc(Color(0xFF212121), 20f, 140f, false, Offset(cx - r * 0.24f, cy + r * 0.08f), Size(r * 0.48f, r * 0.25f), style = Stroke(8f))
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = wonder.displayName.uppercase(),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFEE58),
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}
