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

private enum class HidingSpot(val label: String, val boxColor: Color, val ribbonColor: Color) {
    GIFT_BOX("Gift Box", Color(0xFFFF5252), Color(0xFFFFD600)),
    CLOUD("Fluffy Cloud", Color(0xFFFFFFFF), Color(0xFF40C4FF)),
    BUSH("Garden Bush", Color(0xFF43A047), Color(0xFFFF80AB)),
    BARN("Red Barn", Color(0xFFE53935), Color(0xFFFFF8E1))
}

private enum class PeekCharacter(val displayName: String, val faceColor: Color, val earColor: Color) {
    BUNNY("Bunny", Color(0xFFFFFFFF), Color(0xFFF8BBD0)),
    BEAR("Teddy Bear", Color(0xFFA1887F), Color(0xFF795548)),
    CHICK("Baby Chick", Color(0xFFFFEB3B), Color(0xFFFF9800)),
    PANDA("Panda", Color(0xFFFFFFFF), Color(0xFF263238)),
    MONKEY("Monkey", Color(0xFF8D6E63), Color(0xFFFFCCBC))
}

@Composable
fun PeekABooScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val spots = HidingSpot.entries
    val chars = PeekCharacter.entries

    var spotIndex by remember { mutableIntStateOf(0) }
    var charIndex by remember { mutableIntStateOf(0) }
    var isPeeking by remember { mutableStateOf(false) }
    val popAnim = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val spot = spots[spotIndex]
    val character = chars[charIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak("Where is my friend?")
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> {
                spotIndex = (spotIndex - 1 + spots.size) % spots.size
                isPeeking = false
                soundEngine.play(SoundEffect.PLOP)
                launch { popAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
            }
            ToyboxKey.RIGHT -> {
                spotIndex = (spotIndex + 1) % spots.size
                isPeeking = false
                soundEngine.play(SoundEffect.PLOP)
                launch { popAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
            }
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> {
                if (!isPeeking) {
                    isPeeking = true
                    charIndex = (charIndex + 1) % chars.size
                    soundEngine.play(SoundEffect.PEEKABOO)
                    voiceSpeaker.speak("Peek a boo! ${chars[charIndex].displayName}!")
                    particles.burst(
                        x = canvasWidth * 0.5f,
                        y = canvasHeight * 0.42f,
                        count = 34,
                        baseColor = spot.ribbonColor,
                        shape = ParticleShape.CONFETTI
                    )
                    launch {
                        popAnim.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMedium))
                    }
                } else {
                    isPeeking = false
                    soundEngine.play(SoundEffect.PLOP)
                    voiceSpeaker.speak("Bye bye!")
                    launch {
                        popAnim.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium))
                    }
                }
            }
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
                    listOf(Color(0xFFFFF176), Color(0xFFFFE082), Color(0xFFC8E6C9))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val baseY = size.height * 0.66f
            val u = minOf(size.width, size.height) * 0.23f

            // Pop-out character behind the hiding spot
            val charY = baseY - u * 0.1f - popAnim.value * u * 1.15f
            if (popAnim.value > 0.02f) {
                drawPeekCharacter(character, Offset(cx, charY), u * 0.72f)
            }

            // Hiding spot in front
            drawHidingSpot(spot, Offset(cx, baseY), u, openFraction = popAnim.value)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = if (isPeeking) "PEEK-A-BOO! ${character.displayName.uppercase()}!" else "TAP TO PEEK!",
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A237E),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 12.dp)
        )
    }
}

private fun DrawScope.drawPeekCharacter(ch: PeekCharacter, c: Offset, r: Float) {
    when (ch) {
        PeekCharacter.BUNNY -> {
            drawOval(Color.White, Offset(c.x - r * 0.52f, c.y - r * 1.65f), Size(r * 0.36f, r * 1.1f))
            drawOval(Color.White, Offset(c.x + r * 0.16f, c.y - r * 1.65f), Size(r * 0.36f, r * 1.1f))
            drawOval(ch.earColor, Offset(c.x - r * 0.44f, c.y - r * 1.48f), Size(r * 0.2f, r * 0.78f))
            drawOval(ch.earColor, Offset(c.x + r * 0.24f, c.y - r * 1.48f), Size(r * 0.2f, r * 0.78f))
        }
        else -> {
            drawCircle(ch.earColor, r * 0.34f, Offset(c.x - r * 0.65f, c.y - r * 0.62f))
            drawCircle(ch.earColor, r * 0.34f, Offset(c.x + r * 0.65f, c.y - r * 0.62f))
        }
    }
    drawCircle(ch.faceColor, r * 0.85f, c)
    if (ch == PeekCharacter.PANDA) {
        drawCircle(Color(0xFF263238), r * 0.22f, Offset(c.x - r * 0.3f, c.y - r * 0.14f))
        drawCircle(Color(0xFF263238), r * 0.22f, Offset(c.x + r * 0.3f, c.y - r * 0.14f))
        drawCircle(Color.White, r * 0.08f, Offset(c.x - r * 0.3f, c.y - r * 0.14f))
        drawCircle(Color.White, r * 0.08f, Offset(c.x + r * 0.3f, c.y - r * 0.14f))
    } else {
        drawCircle(Color(0xFF212121), r * 0.11f, Offset(c.x - r * 0.28f, c.y - r * 0.14f))
        drawCircle(Color(0xFF212121), r * 0.11f, Offset(c.x + r * 0.28f, c.y - r * 0.14f))
    }
    drawArc(
        color = Color(0xFF3E2723),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(c.x - r * 0.28f, c.y + r * 0.05f),
        size = Size(r * 0.56f, r * 0.32f),
        style = Stroke(width = 8f)
    )
}

private fun DrawScope.drawHidingSpot(spot: HidingSpot, c: Offset, u: Float, openFraction: Float) {
    when (spot) {
        HidingSpot.CLOUD -> {
            drawCircle(Color.White, u * 0.78f, Offset(c.x, c.y))
            drawCircle(Color.White, u * 0.64f, Offset(c.x - u * 0.72f, c.y + u * 0.12f))
            drawCircle(Color.White, u * 0.64f, Offset(c.x + u * 0.72f, c.y + u * 0.12f))
        }
        HidingSpot.BUSH -> {
            drawCircle(Color(0xFF43A047), u * 0.8f, Offset(c.x, c.y))
            drawCircle(Color(0xFF66BB6A), u * 0.66f, Offset(c.x - u * 0.72f, c.y + u * 0.15f))
            drawCircle(Color(0xFF66BB6A), u * 0.66f, Offset(c.x + u * 0.72f, c.y + u * 0.15f))
            // Little flowers
            drawCircle(Color(0xFFFF80AB), u * 0.12f, Offset(c.x - u * 0.5f, c.y + u * 0.1f))
            drawCircle(Color(0xFFFFEB3B), u * 0.12f, Offset(c.x + u * 0.45f, c.y + u * 0.2f))
            drawCircle(Color(0xFFFF80AB), u * 0.12f, Offset(c.x, c.y + u * 0.35f))
        }
        else -> {
            // Gift box or barn
            val w = u * 1.9f
            val h = u * 1.3f
            drawRoundRect(
                color = spot.boxColor,
                topLeft = Offset(c.x - w / 2f, c.y - h * 0.35f),
                size = Size(w, h),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRect(
                color = spot.ribbonColor,
                topLeft = Offset(c.x - u * 0.16f, c.y - h * 0.35f),
                size = Size(u * 0.32f, h)
            )
            // Lid lifts slightly when peeking
            val lidOffset = openFraction * u * 0.22f
            drawRoundRect(
                color = spot.ribbonColor,
                topLeft = Offset(c.x - w * 0.54f, c.y - h * 0.52f - lidOffset),
                size = Size(w * 1.08f, u * 0.28f),
                cornerRadius = CornerRadius(16f, 16f)
            )
        }
    }
}
