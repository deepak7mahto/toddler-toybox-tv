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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class SpiralSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun ChakkarSpinnerScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val themes = remember {
        listOf(
            listOf(Color(0xFFFFD600), Color(0xFFFF9800), Color(0xFFFF5252)),
            listOf(Color(0xFF40C4FF), Color(0xFF69F0AE), Color(0xFFFFFF8D)),
            listOf(Color(0xFFFF80AB), Color(0xFFE040FB), Color(0xFFFFD600)),
            listOf(Color(0xFFFF5252), Color(0xFFFFD600), Color(0xFF40C4FF), Color(0xFF69F0AE))
        )
    }

    var themeIndex by remember { mutableIntStateOf(0) }
    var spinSpeed by remember { mutableFloatStateOf(4.2f) }
    var angle by remember { mutableFloatStateOf(0f) }
    var pressCount by remember { mutableIntStateOf(0) }
    val sparks = remember { mutableStateListOf<SpiralSpark>() }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> themeIndex = (themeIndex - 1 + themes.size) % themes.size
            ToyboxKey.RIGHT -> themeIndex = (themeIndex + 1) % themes.size
            else -> themeIndex = (themeIndex + 1) % themes.size
        }
        spinSpeed = (spinSpeed + 5.5f).coerceAtMost(22f)
        soundEngine.play(SoundEffect.CHAKKAR_SPIN, rate = 0.85f + (themeIndex * 0.1f))
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.54f,
            count = 30,
            baseColor = themes[themeIndex].first(),
            speed = 560f,
            shape = ParticleShape.STAR
        )
        pressCount++
        if (pressCount % 5 == 0) {
            voiceSpeaker.speak("Whee! Spin spin!")
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                angle += spinSpeed * dt
                spinSpeed = (spinSpeed - dt * 2.2f).coerceAtLeast(3.2f)

                val cx = canvasWidth * 0.5f
                val cy = canvasHeight * 0.54f
                val palette = themes[themeIndex]
                val jets = if (spinSpeed > 10f) 4 else 2
                repeat(jets) { j ->
                    if (sparks.size < 220) {
                        val jetAngle = angle + j * (2f * PI.toFloat() / jets)
                        val tangent = jetAngle + (PI.toFloat() * 0.42f)
                        val spd = 340f + spinSpeed * 26f + Random.nextFloat() * 140f
                        val life = 0.55f + Random.nextFloat() * 0.35f
                        sparks.add(
                            SpiralSpark(
                                x = cx + cos(jetAngle) * 56f,
                                y = cy + sin(jetAngle) * 56f,
                                vx = cos(tangent) * spd,
                                vy = sin(tangent) * spd,
                                life = life,
                                maxLife = life,
                                color = palette[Random.nextInt(palette.size)],
                                radius = 7f + Random.nextFloat() * 8f
                            )
                        )
                    }
                }

                val iter = sparks.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.life -= dt
                    if (s.life <= 0f) {
                        iter.remove()
                    } else {
                        s.x += s.vx * dt
                        s.y += s.vy * dt
                        s.vx *= 0.985f
                        s.vy *= 0.985f
                    }
                }

                particles.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF28194A), Color(0xFF120B29), Color(0xFF070414))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.54f
            val palette = themes[themeIndex]

            // Glowing floor aura
            drawCircle(
                color = palette.first().copy(alpha = (0.14f + (spinSpeed / 22f) * 0.2f).coerceIn(0f, 0.35f)),
                radius = minOf(size.width, size.height) * 0.42f,
                center = Offset(cx, cy)
            )

            // Spiraling spark tails
            for (s in sparks) {
                val a = (s.life / s.maxLife).coerceIn(0f, 1f)
                drawLine(
                    color = s.color.copy(alpha = a),
                    start = Offset(s.x, s.y),
                    end = Offset(s.x - s.vx * 0.045f, s.y - s.vy * 0.045f),
                    strokeWidth = s.radius * a
                )
                drawCircle(Color.White.copy(alpha = a), s.radius * 0.45f * a, Offset(s.x, s.y))
            }

            // Spinning Chakkar wheel in center
            val wheelR = minOf(size.width, size.height) * 0.11f
            drawCircle(Color(0xFFFF5252), wheelR, Offset(cx, cy))
            drawCircle(Color(0xFFFFD600), wheelR * 0.72f, Offset(cx, cy), style = Stroke(width = 14f))
            for (spoke in 0 until 6) {
                val sa = angle + spoke * (PI.toFloat() / 3f)
                drawLine(
                    color = Color.White,
                    start = Offset(cx, cy),
                    end = Offset(cx + cos(sa) * wheelR * 0.85f, cy + sin(sa) * wheelR * 0.85f),
                    strokeWidth = 8f
                )
            }
            drawCircle(Color.White, wheelR * 0.22f, Offset(cx, cy))

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = "CHAKKAR SPINNER!",
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
