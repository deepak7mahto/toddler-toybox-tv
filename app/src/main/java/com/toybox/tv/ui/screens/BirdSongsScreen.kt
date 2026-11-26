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

private enum class BirdFriend(
    val displayName: String,
    val sound: SoundEffect,
    val bodyColor: Color,
    val wingColor: Color,
    val beakColor: Color
) {
    SONGBIRD("Bluebird", SoundEffect.REAL_SONGBIRD, Color(0xFF42A5F5), Color(0xFF1E88E5), Color(0xFFFFCA28)),
    ROBIN("Forest Bird", SoundEffect.REAL_BIRD_CHIRP, Color(0xFFFF7043), Color(0xFFD84315), Color(0xFFFFEE58)),
    CROW("Clever Crow", SoundEffect.REAL_CROW, Color(0xFF37474F), Color(0xFF263238), Color(0xFFFFCA28)),
    OWL("Night Owl", SoundEffect.REAL_OWL, Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFFFFCA28)),
    DUCK("Little Duck", SoundEffect.REAL_DUCK, Color(0xFFFFEB3B), Color(0xFFFBC02D), Color(0xFFFF7043)),
    BEE("Bumblebee", SoundEffect.REAL_BEE, Color(0xFFFFD600), Color(0xFFE1F5FE), Color(0xFF212121))
}

@Composable
fun BirdSongsScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val birds = BirdFriend.entries
    var idx by remember { mutableIntStateOf(0) }
    val flap = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }
    val beakPath = remember { Path() }

    val bird = birds[idx]

    LaunchedEffect(Unit) {
        soundEngine.play(bird.sound)
        voiceSpeaker.speak(bird.displayName)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + birds.size) % birds.size
            ToyboxKey.RIGHT -> idx = (idx + 1) % birds.size
            else -> Unit
        }
        val active = birds[idx]
        soundEngine.play(active.sound)
        voiceSpeaker.speak(active.displayName)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.44f,
            count = 30,
            baseColor = active.bodyColor,
            shape = ParticleShape.STAR
        )
        launch {
            flap.snapTo(1f)
            flap.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
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
            .background(Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC), Color(0xFFDCEDC8))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Cozy wooden tree branch
            drawLine(
                color = Color(0xFF6D4C41),
                start = Offset(0f, size.height * 0.72f),
                end = Offset(size.width, size.height * 0.64f),
                strokeWidth = 34f
            )
            // Leaves on branch
            drawOval(Color(0xFF66BB6A), Offset(size.width * 0.14f, size.height * 0.64f), Size(110f, 56f))
            drawOval(Color(0xFF66BB6A), Offset(size.width * 0.78f, size.height * 0.58f), Size(120f, 60f))

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f - flap.value * 95f
            val r = minOf(size.width, size.height) * 0.22f

            // Flapping wings
            val wingLift = flap.value * r * 0.7f
            drawOval(bird.wingColor, Offset(cx - r * 1.48f, cy - r * 0.35f - wingLift), Size(r * 0.85f, r * 0.52f))
            drawOval(bird.wingColor, Offset(cx + r * 0.63f, cy - r * 0.35f - wingLift), Size(r * 0.85f, r * 0.52f))

            // Round chubby bird body
            drawCircle(bird.bodyColor, r * 0.88f, Offset(cx, cy))

            if (bird == BirdFriend.BEE) {
                drawRect(Color(0xFF212121), Offset(cx - r * 0.78f, cy - r * 0.15f), Size(r * 1.56f, r * 0.2f))
                drawRect(Color(0xFF212121), Offset(cx - r * 0.72f, cy + r * 0.25f), Size(r * 1.44f, r * 0.2f))
            }

            // Cute eyes & beak
            drawCircle(Color.White, r * 0.2f, Offset(cx - r * 0.28f, cy - r * 0.22f))
            drawCircle(Color.White, r * 0.2f, Offset(cx + r * 0.28f, cy - r * 0.22f))
            drawCircle(Color(0xFF212121), r * 0.11f, Offset(cx - r * 0.28f, cy - r * 0.22f))
            drawCircle(Color(0xFF212121), r * 0.11f, Offset(cx + r * 0.28f, cy - r * 0.22f))

            beakPath.reset()
            beakPath.moveTo(cx - r * 0.18f, cy + r * 0.02f)
            beakPath.lineTo(cx + r * 0.18f, cy + r * 0.02f)
            beakPath.lineTo(cx, cy + r * 0.34f)
            beakPath.close()
            drawPath(beakPath, bird.beakColor)

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = bird.displayName.uppercase(),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20),
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}
