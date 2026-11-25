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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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

private enum class DinoFriend(
    val displayName: String,
    val phrase: String,
    val bodyColor: Color,
    val spikeColor: Color
) {
    T_REX("T-Rex", "T Rex! Stomp! Roar!", Color(0xFF66BB6A), Color(0xFFFFCA28)),
    BRONTO("Long Neck", "Brontosaurus! Stomp stomp!", Color(0xFF42A5F5), Color(0xFF81D4FA)),
    STEGO("Stegosaurus", "Stegosaurus! Spike spike!", Color(0xFFFF7043), Color(0xFFFFEE58)),
    TRICERA("Triceratops", "Triceratops! Horn horn!", Color(0xFFAB47BC), Color(0xFFF8BBD0)),
    BABY_EGG("Baby Dino", "Crack! Baby Dino hatched!", Color(0xFF26A69A), Color(0xFFFFF8E1))
}

@Composable
fun DinosaurStompScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val dinos = DinoFriend.entries
    var idx by remember { mutableIntStateOf(0) }
    val stomp = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val dino = dinos[idx]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(dino.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + dinos.size) % dinos.size
            ToyboxKey.RIGHT -> idx = (idx + 1) % dinos.size
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> Unit
        }
        val active = dinos[idx]
        soundEngine.play(SoundEffect.DINO_STOMP, rate = 0.9f + (idx % 3) * 0.12f)
        voiceSpeaker.speak(active.phrase)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.68f,
            count = 30,
            baseColor = active.spikeColor,
            shape = ParticleShape.STAR
        )
        launch {
            stomp.snapTo(1f)
            stomp.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
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
            .background(Brush.verticalGradient(listOf(Color(0xFFFFCC80), Color(0xFFFFE082), Color(0xFFA5D6A7))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Jungle ground
            drawRect(Color(0xFF4CAF50), Offset(0f, size.height * 0.72f), Size(size.width, size.height * 0.28f))

            val cx = size.width * 0.5f
            val cy = size.height * 0.54f - stomp.value * size.height * 0.09f
            val r = minOf(size.width, size.height) * 0.24f

            drawDino(dino, Offset(cx, cy), r)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = dino.displayName.uppercase(),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}

private fun DrawScope.drawDino(d: DinoFriend, c: Offset, r: Float) {
    if (d == DinoFriend.BABY_EGG) {
        // Speckled hatching egg + peeking baby dino
        drawCircle(d.bodyColor, r * 0.58f, Offset(c.x, c.y - r * 0.45f))
        drawCircle(Color.White, r * 0.14f, Offset(c.x - r * 0.2f, c.y - r * 0.52f))
        drawCircle(Color.White, r * 0.14f, Offset(c.x + r * 0.2f, c.y - r * 0.52f))
        drawCircle(Color(0xFF212121), r * 0.08f, Offset(c.x - r * 0.2f, c.y - r * 0.52f))
        drawCircle(Color(0xFF212121), r * 0.08f, Offset(c.x + r * 0.2f, c.y - r * 0.52f))
        drawArc(d.spikeColor, 0f, 180f, true, Offset(c.x - r * 0.78f, c.y - r * 0.55f), Size(r * 1.56f, r * 1.45f))
        return
    }

    // Tail
    val tail = Path().apply {
        moveTo(c.x - r * 0.6f, c.y + r * 0.1f)
        lineTo(c.x - r * 1.55f, c.y + r * 0.45f)
        lineTo(c.x - r * 0.5f, c.y + r * 0.55f)
        close()
    }
    drawPath(tail, d.bodyColor)

    // Back spikes
    for (s in 0..3) {
        drawCircle(d.spikeColor, r * 0.18f, Offset(c.x - r * 0.65f + s * r * 0.38f, c.y - r * 0.42f))
    }

    // Body & legs
    drawOval(d.bodyColor, Offset(c.x - r * 0.95f, c.y - r * 0.35f), Size(r * 1.85f, r * 1.05f))
    drawRoundRect(d.bodyColor, Offset(c.x - r * 0.65f, c.y + r * 0.45f), Size(r * 0.38f, r * 0.55f), CornerRadius(14f, 14f))
    drawRoundRect(d.bodyColor, Offset(c.x + r * 0.22f, c.y + r * 0.45f), Size(r * 0.38f, r * 0.55f), CornerRadius(14f, 14f))

    // Neck & friendly head
    val neckH = if (d == DinoFriend.BRONTO) r * 1.15f else r * 0.55f
    drawRoundRect(d.bodyColor, Offset(c.x + r * 0.48f, c.y - neckH), Size(r * 0.42f, neckH + r * 0.1f), CornerRadius(18f, 18f))
    drawOval(d.bodyColor, Offset(c.x + r * 0.38f, c.y - neckH - r * 0.32f), Size(r * 0.95f, r * 0.68f))
    drawCircle(Color.White, r * 0.14f, Offset(c.x + r * 0.98f, c.y - neckH - r * 0.06f))
    drawCircle(Color(0xFF212121), r * 0.08f, Offset(c.x + r * 1.0f, c.y - neckH - r * 0.06f))
}
