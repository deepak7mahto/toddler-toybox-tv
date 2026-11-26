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

private enum class NatureScene(
    val title: String,
    val sound: SoundEffect,
    val topColor: Color,
    val bottomColor: Color,
    val accent: Color
) {
    OCEAN_WAVES("Ocean Waves", SoundEffect.REAL_WAVES, Color(0xFF0288D1), Color(0xFF4FC3F7), Color(0xFFFFE082)),
    HEAVY_RAIN("Rain Shower", SoundEffect.REAL_RAIN, Color(0xFF455A64), Color(0xFF78909C), Color(0xFF81D4FA)),
    THUNDER("Thunder Boom", SoundEffect.REAL_THUNDER, Color(0xFF263238), Color(0xFF37474F), Color(0xFFFFEE58)),
    SPRING_FOREST("Forest Morning", SoundEffect.REAL_BIRD_CHIRP, Color(0xFF43A047), Color(0xFFA5D6A7), Color(0xFFFFEB3B)),
    NIGHT_OWLS("Night Forest", SoundEffect.REAL_OWL, Color(0xFF1A237E), Color(0xFF283593), Color(0xFFFFF59D))
}

@Composable
fun NatureSoundsScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val scenes = NatureScene.entries
    var idx by remember { mutableIntStateOf(0) }
    val flash = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val scene = scenes[idx]

    LaunchedEffect(Unit) {
        soundEngine.play(scene.sound)
        voiceSpeaker.speak(scene.title)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + scenes.size) % scenes.size
            ToyboxKey.RIGHT, ToyboxKey.OK -> idx = (idx + 1) % scenes.size
            else -> Unit
        }
        val active = scenes[idx]
        soundEngine.play(active.sound)
        voiceSpeaker.speak(active.title)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.45f,
            count = 30,
            baseColor = active.accent,
            shape = ParticleShape.CIRCLE
        )
        launch {
            flash.snapTo(1f)
            flash.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow))
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
            .background(Brush.verticalGradient(listOf(scene.topColor, scene.bottomColor)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val w = size.width
            val h = size.height

            when (scene) {
                NatureScene.OCEAN_WAVES -> {
                    drawCircle(Color(0xFFFFEE58), h * 0.11f, Offset(w * 0.82f, h * 0.18f))
                    for (layer in 0 until 3) {
                        val baseY = h * (0.58f + layer * 0.11f)
                        val col = if (layer == 0) Color(0xFF039BE5) else if (layer == 1) Color(0xFF0277BD) else Color(0xFF01579B)
                        for (x in 0..10) {
                            val cx = x * (w / 10f)
                            val cy = baseY + sin(elapsed * 2.5f + x + layer) * 24f
                            drawCircle(col, w * 0.09f, Offset(cx, cy))
                        }
                        drawRect(col, Offset(0f, baseY), Size(w, h - baseY))
                    }
                }
                NatureScene.HEAVY_RAIN -> {
                    for (d in 0 until 36) {
                        val dx = ((d * 71 + 19) % 96 + 2) / 100f * w
                        val dy = ((elapsed * 0.85f + d * 0.13f) % 1f) * h
                        drawLine(Color(0xFFB3E5FC), Offset(dx, dy), Offset(dx - 10f, dy + 40f), 7f)
                    }
                    drawCircle(Color(0xFFCFD8DC), h * 0.14f, Offset(w * 0.5f, h * 0.24f))
                    drawCircle(Color(0xFFECEFF1), h * 0.12f, Offset(w * 0.4f, h * 0.27f))
                    drawCircle(Color(0xFFECEFF1), h * 0.12f, Offset(w * 0.6f, h * 0.27f))
                }
                NatureScene.THUNDER -> {
                    if (flash.value > 0.25f) {
                        drawRect(Color.White.copy(alpha = flash.value * 0.35f))
                    }
                    // Lightning bolt
                    drawLine(Color(0xFFFFEE58), Offset(w * 0.52f, h * 0.28f), Offset(w * 0.45f, h * 0.52f), 22f)
                    drawLine(Color(0xFFFFEE58), Offset(w * 0.45f, h * 0.52f), Offset(w * 0.54f, h * 0.52f), 22f)
                    drawLine(Color(0xFFFFEE58), Offset(w * 0.54f, h * 0.52f), Offset(w * 0.47f, h * 0.76f), 22f)
                    drawCircle(Color(0xFF78909C), h * 0.15f, Offset(w * 0.5f, h * 0.22f))
                }
                NatureScene.SPRING_FOREST -> {
                    drawCircle(Color(0xFFFFEE58), h * 0.11f, Offset(w * 0.18f, h * 0.2f))
                    for (t in 0 until 4) {
                        val tx = w * (0.18f + t * 0.21f)
                        drawRect(Color(0xFF6D4C41), Offset(tx - 18f, h * 0.58f), Size(36f, h * 0.32f))
                        drawCircle(Color(0xFF2E7D32), h * 0.14f, Offset(tx, h * 0.52f))
                    }
                }
                NatureScene.NIGHT_OWLS -> {
                    drawCircle(Color(0xFFFFF59D), h * 0.12f, Offset(w * 0.8f, h * 0.22f))
                    for (s in 0 until 20) {
                        val sx = ((s * 149 + 37) % 92 + 4) / 100f * w
                        val sy = ((s * 83 + 11) % 58 + 5) / 100f * h
                        ParticleSystem.drawStar(this, Offset(sx, sy), 12f + (s % 3) * 6f, Color(0xFFFFEE58))
                    }
                }
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = scene.title.uppercase(),
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
