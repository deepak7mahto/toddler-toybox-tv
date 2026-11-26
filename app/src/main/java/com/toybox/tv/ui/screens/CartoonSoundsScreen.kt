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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlinx.coroutines.launch

private enum class SillySound(
    val title: String,
    val sound: SoundEffect,
    val color: Color,
    val bgTop: Color
) {
    BOING("Spring Boing!", SoundEffect.REAL_BOING, Color(0xFFFF5252), Color(0xFFFFEBEE)),
    CORK_POP("Pop Pop!", SoundEffect.REAL_CORK_POP, Color(0xFFFF9800), Color(0xFFFFF3E0)),
    COWBELL("Cowbell!", SoundEffect.REAL_COWBELL, Color(0xFFFFCA28), Color(0xFFFFFDE7)),
    BUGLE("Trumpet!", SoundEffect.REAL_BELL, Color(0xFF42A5F5), Color(0xFFE3F2FD)),
    SQUEAK("Squeaky Mouse!", SoundEffect.REAL_MOUSE, Color(0xFFAB47BC), Color(0xFFF3E5F5)),
    WOOD("Clack Clack!", SoundEffect.REAL_WOOD_PLANK, Color(0xFF26A69A), Color(0xFFE0F2F1)),
    SIREN("Wee Woo!", SoundEffect.REAL_SIREN, Color(0xFFE53935), Color(0xFFFFEBEE))
}

@Composable
fun CartoonSoundsScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val items = SillySound.entries
    var idx by remember { mutableIntStateOf(0) }
    val squash = remember { Animatable(0f) }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val item = items[idx]

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + items.size) % items.size
            ToyboxKey.RIGHT, ToyboxKey.OK -> idx = (idx + 1) % items.size
            else -> Unit
        }
        val active = items[idx]
        soundEngine.play(active.sound)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.48f,
            count = 36,
            baseColor = active.color,
            speed = 540f,
            shape = ParticleShape.CONFETTI
        )
        launch {
            squash.snapTo(1f)
            squash.animateTo(0f, spring(dampingRatio = 0.28f, stiffness = Spring.StiffnessMedium))
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
            .background(Brush.verticalGradient(listOf(item.bgTop, Color.White)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f
            val r = minOf(size.width, size.height) * 0.24f
            val rx = r * (1f + squash.value * 0.28f)
            val ry = r * (1f - squash.value * 0.18f)

            // Big comic burst halo
            ParticleSystem.drawStar(this, Offset(cx, cy), r * (1.35f + squash.value * 0.35f), item.color.copy(alpha = 0.25f))

            // Squash-and-stretch bouncy cartoon badge
            drawRoundRect(
                color = item.color,
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f),
                cornerRadius = CornerRadius(64f, 64f)
            )
            // Silly smiling eyes
            drawCircle(Color.White, r * 0.2f, Offset(cx - r * 0.32f, cy - r * 0.16f))
            drawCircle(Color.White, r * 0.2f, Offset(cx + r * 0.32f, cy - r * 0.16f))
            drawCircle(Color(0xFF212121), r * 0.1f, Offset(cx - r * 0.32f, cy - r * 0.16f))
            drawCircle(Color(0xFF212121), r * 0.1f, Offset(cx + r * 0.32f, cy - r * 0.16f))
            drawArc(Color.White, 10f, 160f, false, Offset(cx - r * 0.36f, cy + r * 0.08f), Size(r * 0.72f, r * 0.4f), style = Stroke(12f))

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = item.title.uppercase(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = item.color,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}
