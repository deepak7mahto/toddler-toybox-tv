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
import androidx.compose.ui.graphics.Path
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

private data class FountainSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val size: Float
)

@Composable
fun AnarFountainScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine
) {
    val coneColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFF9800),
            Color(0xFFFFD600),
            Color(0xFF66BB6A),
            Color(0xFF42A5F5)
        )
    }
    val sparkColors = remember {
        listOf(
            Color(0xFFFFEE58),
            Color(0xFFFFD600),
            Color(0xFFFFAB40),
            Color(0xFFFF80AB),
            Color(0xFF80D8FF),
            Color.White
        )
    }

    val coneTimers = remember { mutableStateListOf(0.8f, 0f, 1.2f, 0f, 0.8f) }
    val sparks = remember { mutableStateListOf<FountainSpark>() }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }
    val conePath = remember { Path() }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val key = lastKeyEvent.second
        if (key == ToyboxKey.OK) {
            // Grand Finale — ignite ALL 5 Anar cones at once!
            for (i in coneTimers.indices) coneTimers[i] = 2.6f
            soundEngine.play(SoundEffect.ANAR_FOUNTAIN, rate = 1.05f)
            soundEngine.play(SoundEffect.CHIME)
            particles.burst(canvasWidth * 0.5f, canvasHeight * 0.45f, 36, Color(0xFFFFD600), 540f, ParticleShape.STAR)
        } else {
            val idx = when (key) {
                ToyboxKey.LEFT -> 0
                ToyboxKey.UP -> 1
                ToyboxKey.DOWN -> 3
                ToyboxKey.RIGHT -> 4
                ToyboxKey.OK -> 2
            }
            coneTimers[idx] = 2.4f
            soundEngine.play(SoundEffect.ANAR_FOUNTAIN, rate = 0.88f + idx * 0.08f)
            val cx = canvasWidth * (0.14f + idx * 0.18f)
            particles.burst(cx, canvasHeight * 0.65f, 22, coneColors[idx], 460f, ParticleShape.STAR)
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now

                for (i in coneTimers.indices) {
                    if (coneTimers[i] > 0f) {
                        coneTimers[i] = (coneTimers[i] - dt).coerceAtLeast(0f)
                        val cx = canvasWidth * (0.14f + i * 0.18f)
                        val topY = canvasHeight * 0.72f
                        repeat(3) {
                            if (sparks.size < 280) {
                                val life = 0.75f + Random.nextFloat() * 0.55f
                                sparks.add(
                                    FountainSpark(
                                        x = cx + (Random.nextFloat() - 0.5f) * 18f,
                                        y = topY,
                                        vx = (Random.nextFloat() - 0.5f) * 320f,
                                        vy = -780f - Random.nextFloat() * 440f,
                                        life = life,
                                        maxLife = life,
                                        color = if (Random.nextFloat() < 0.55f) sparkColors[Random.nextInt(sparkColors.size)] else coneColors[i],
                                        size = 7f + Random.nextFloat() * 7f
                                    )
                                )
                            }
                        }
                    }
                }

                val iter = sparks.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.life -= dt
                    if (s.life <= 0f) {
                        iter.remove()
                    } else {
                        s.vy += 980f * dt
                        s.x += s.vx * dt
                        s.y += s.vy * dt
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
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0E2A), Color(0xFF1A1446), Color(0xFF2B1B4D))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Night ground
            drawRect(Color(0xFF120B24), Offset(0f, size.height * 0.86f), androidx.compose.ui.geometry.Size(size.width, size.height * 0.14f))

            // Fountain sparks
            for (s in sparks) {
                val a = (s.life / s.maxLife).coerceIn(0f, 1f)
                drawLine(
                    color = s.color.copy(alpha = a),
                    start = Offset(s.x, s.y),
                    end = Offset(s.x - s.vx * 0.035f, s.y - s.vy * 0.035f),
                    strokeWidth = s.size * a
                )
                drawCircle(Color.White.copy(alpha = a), s.size * 0.42f * a, Offset(s.x, s.y))
            }

            // 5 festive Anar cones
            for (i in 0 until 5) {
                val cx = size.width * (0.14f + i * 0.18f)
                val baseY = size.height * 0.87f
                val tipY = size.height * 0.72f
                val halfW = size.width * 0.038f

                if (coneTimers[i] > 0f) {
                    drawCircle(
                        color = Color(0xFFFFEE58).copy(alpha = 0.45f),
                        radius = 48f,
                        center = Offset(cx, tipY)
                    )
                }

                conePath.reset()
                conePath.moveTo(cx, tipY)
                conePath.lineTo(cx - halfW, baseY)
                conePath.lineTo(cx + halfW, baseY)
                conePath.close()
                drawPath(conePath, coneColors[i])
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = "ANAR FOUNTAIN! (OK = ALL 5)",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFEE58),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 10.dp)
        )
    }
}
