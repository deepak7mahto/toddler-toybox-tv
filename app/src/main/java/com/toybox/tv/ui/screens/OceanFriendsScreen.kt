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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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

private enum class SeaCreature(
    val displayName: String,
    val phrase: String,
    val bodyColor: Color,
    val accentColor: Color
) {
    WHALE("Whale", "Whale! Splash!", Color(0xFF42A5F5), Color(0xFFB3E5FC)),
    OCTOPUS("Octopus", "Octopus! Wiggle wiggle!", Color(0xFFAB47BC), Color(0xFFF8BBD0)),
    FISH("Clownfish", "Fish! Blub blub!", Color(0xFFFF7043), Color(0xFFFFFFFF)),
    TURTLE("Sea Turtle", "Turtle! Swim swim!", Color(0xFF66BB6A), Color(0xFF2E7D32)),
    STARFISH("Starfish", "Starfish! Twinkle!", Color(0xFFFFCA28), Color(0xFFFF7043)),
    CRAB("Happy Crab", "Crab! Snip snip!", Color(0xFFEF5350), Color(0xFFFFCDD2))
}

@Composable
fun OceanFriendsScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val creatures = SeaCreature.entries
    var currentIndex by remember { mutableIntStateOf(0) }
    val splashBounce = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var elapsedSec by remember { mutableStateOf(0f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val creature = creatures[currentIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(creature.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> currentIndex = (currentIndex - 1 + creatures.size) % creatures.size
            ToyboxKey.RIGHT -> currentIndex = (currentIndex + 1) % creatures.size
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> {
                // Splash & swim!
            }
        }
        val active = creatures[currentIndex]
        soundEngine.play(SoundEffect.WATER_SPLASH, rate = 0.9f + (currentIndex % 3) * 0.15f)
        voiceSpeaker.speak(active.phrase)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.48f,
            count = 32,
            baseColor = Color(0xFFB3E5FC),
            shape = ParticleShape.CIRCLE
        )
        launch {
            splashBounce.snapTo(1f)
            splashBounce.animateTo(0f, spring(dampingRatio = 0.36f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                elapsedSec += dt
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
                    listOf(Color(0xFF0288D1), Color(0xFF0277BD), Color(0xFF01579B))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Sandy ocean floor
            drawRect(Color(0xFFFFE082), Offset(0f, size.height * 0.85f), Size(size.width, size.height * 0.15f))

            // Rising ambient bubbles
            for (b in 0 until 12) {
                val bx = ((b * 131 + 47) % 92 + 4) / 100f * size.width
                val by = ((1f - ((elapsedSec * 0.14f + b * 0.19f) % 1f)) * size.height)
                drawCircle(Color.White.copy(alpha = 0.25f), radius = 10f + (b % 3) * 7f, center = Offset(bx, by), style = Stroke(3f))
            }

            // Bobbing sea creature in center
            val bobY = sin(elapsedSec * 2.4f) * 20f - splashBounce.value * size.height * 0.11f
            val cx = size.width * 0.5f
            val cy = size.height * 0.48f + bobY
            val r = minOf(size.width, size.height) * 0.25f * (1f + splashBounce.value * 0.12f)

            drawSeaCreature(creature, Offset(cx, cy), r)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = creature.displayName.uppercase(),
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

private fun DrawScope.drawSeaCreature(c: SeaCreature, center: Offset, r: Float) {
    when (c) {
        SeaCreature.WHALE -> {
            // Tail
            val tail = Path().apply {
                moveTo(center.x - r * 0.9f, center.y)
                lineTo(center.x - r * 1.55f, center.y - r * 0.48f)
                lineTo(center.x - r * 1.45f, center.y + r * 0.42f)
                close()
            }
            drawPath(tail, c.bodyColor)
            drawOval(c.bodyColor, Offset(center.x - r * 1.1f, center.y - r * 0.72f), Size(r * 2.2f, r * 1.45f))
            drawOval(c.accentColor, Offset(center.x - r * 0.5f, center.y + r * 0.15f), Size(r * 1.4f, r * 0.52f))
            // Water spout
            drawLine(c.accentColor, Offset(center.x + r * 0.2f, center.y - r * 0.72f), Offset(center.x + r * 0.2f, center.y - r * 1.25f), 12f)
            drawCircle(c.accentColor, r * 0.16f, Offset(center.x + r * 0.02f, center.y - r * 1.28f))
            drawCircle(c.accentColor, r * 0.16f, Offset(center.x + r * 0.38f, center.y - r * 1.28f))
            drawSeaEyes(Offset(center.x + r * 0.32f, center.y - r * 0.1f), r)
        }
        SeaCreature.OCTOPUS -> {
            for (i in -3..3) {
                drawOval(c.bodyColor, Offset(center.x + i * r * 0.26f - r * 0.12f, center.y + r * 0.22f), Size(r * 0.24f, r * 0.85f))
            }
            drawCircle(c.bodyColor, r * 0.85f, center)
            drawSeaEyes(center, r)
        }
        SeaCreature.STARFISH -> {
            ParticleSystem.drawStar(this, center, r * 1.18f, c.bodyColor)
            drawSeaEyes(center, r * 0.8f)
        }
        SeaCreature.TURTLE -> {
            drawCircle(c.bodyColor, r * 0.36f, Offset(center.x + r * 0.95f, center.y - r * 0.08f))
            drawOval(c.bodyColor, Offset(center.x - r * 0.75f, center.y + r * 0.42f), Size(r * 0.42f, r * 0.26f))
            drawOval(c.bodyColor, Offset(center.x + r * 0.32f, center.y + r * 0.42f), Size(r * 0.42f, r * 0.26f))
            drawOval(c.accentColor, Offset(center.x - r * 0.95f, center.y - r * 0.65f), Size(r * 1.8f, r * 1.25f))
            drawSeaEyes(Offset(center.x + r * 0.95f, center.y - r * 0.08f), r * 0.5f)
        }
        SeaCreature.CRAB -> {
            drawCircle(c.bodyColor, r * 0.3f, Offset(center.x - r * 0.95f, center.y - r * 0.62f))
            drawCircle(c.bodyColor, r * 0.3f, Offset(center.x + r * 0.95f, center.y - r * 0.62f))
            drawOval(c.bodyColor, Offset(center.x - r * 0.95f, center.y - r * 0.55f), Size(r * 1.9f, r * 1.25f))
            drawSeaEyes(center, r)
        }
        SeaCreature.FISH -> {
            val tail = Path().apply {
                moveTo(center.x - r * 0.85f, center.y)
                lineTo(center.x - r * 1.48f, center.y - r * 0.55f)
                lineTo(center.x - r * 1.48f, center.y + r * 0.55f)
                close()
            }
            drawPath(tail, c.bodyColor)
            drawOval(c.bodyColor, Offset(center.x - r * 1.0f, center.y - r * 0.72f), Size(r * 2.0f, r * 1.44f))
            drawRect(c.accentColor, Offset(center.x - r * 0.22f, center.y - r * 0.68f), Size(r * 0.26f, r * 1.36f))
            drawSeaEyes(Offset(center.x + r * 0.35f, center.y - r * 0.1f), r * 0.75f)
        }
    }
}

private fun DrawScope.drawSeaEyes(c: Offset, r: Float) {
    drawCircle(Color.White, r * 0.16f, Offset(c.x - r * 0.24f, c.y - r * 0.1f))
    drawCircle(Color.White, r * 0.16f, Offset(c.x + r * 0.24f, c.y - r * 0.1f))
    drawCircle(Color(0xFF212121), r * 0.09f, Offset(c.x - r * 0.24f, c.y - r * 0.1f))
    drawCircle(Color(0xFF212121), r * 0.09f, Offset(c.x + r * 0.24f, c.y - r * 0.1f))
}
