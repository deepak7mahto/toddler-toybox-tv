package com.toybox.tv.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import kotlin.math.sin
import kotlin.random.Random

data class FloatingBalloon(
    val id: Int,
    var xFraction: Float,
    var yFraction: Float,
    val speed: Float,
    val radiusDp: Float,
    val color: Color,
    val wobblePhase: Float,
    val isStar: Boolean = false
)

private data class SurpriseSticker(
    var xFraction: Float,
    var yFraction: Float,
    var life: Float,
    val kind: Int // 0 = sun, 1 = rainbow, 2 = star
)

@Composable
fun BalloonPopScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val balloonColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFFCA28),
            Color(0xFF40C4FF),
            Color(0xFF66BB6A),
            Color(0xFFFF80AB),
            Color(0xFFAB47BC),
            Color(0xFFFF9800)
        )
    }

    var nextId by remember { mutableIntStateOf(1) }
    var popCount by remember { mutableIntStateOf(0) }
    val balloons = remember {
        mutableStateListOf<FloatingBalloon>().apply {
            repeat(9) { idx ->
                add(
                    FloatingBalloon(
                        id = idx + 1,
                        xFraction = 0.12f + (idx % 5) * 0.19f + Random.nextFloat() * 0.05f,
                        yFraction = 0.25f + Random.nextFloat() * 0.85f,
                        speed = 0.09f + Random.nextFloat() * 0.07f,
                        radiusDp = 64f + Random.nextFloat() * 24f,
                        color = balloonColors[idx % balloonColors.size],
                        wobblePhase = Random.nextFloat() * 6.28f,
                        isStar = idx == 4
                    )
                )
            }
            nextId = 10
        }
    }

    val particles = remember { ParticleSystem() }
    var sticker by remember { mutableStateOf<SurpriseSticker?>(null) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    fun spawnBalloon(startAtBottom: Boolean = true): FloatingBalloon {
        val id = nextId++
        return FloatingBalloon(
            id = id,
            xFraction = 0.10f + Random.nextFloat() * 0.80f,
            yFraction = if (startAtBottom) 1.15f + Random.nextFloat() * 0.25f else 0.2f + Random.nextFloat() * 0.7f,
            speed = 0.085f + Random.nextFloat() * 0.075f,
            radiusDp = 62f + Random.nextFloat() * 26f,
            color = balloonColors[Random.nextInt(balloonColors.size)],
            wobblePhase = Random.nextFloat() * 6.28f,
            isStar = Random.nextFloat() < 0.18f
        )
    }

    // Pop a balloon on ANY remote key press!
    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val key = lastKeyEvent.second
        val visible = balloons.filter { it.yFraction in 0.08f..0.98f }
        val target = when (key) {
            ToyboxKey.LEFT -> visible.minByOrNull { it.xFraction }
            ToyboxKey.RIGHT -> visible.maxByOrNull { it.xFraction }
            ToyboxKey.UP -> visible.minByOrNull { it.yFraction }
            ToyboxKey.DOWN -> visible.maxByOrNull { it.yFraction }
            ToyboxKey.OK -> visible.minByOrNull {
                val dx = it.xFraction - 0.5f
                val dy = it.yFraction - 0.5f
                dx * dx + dy * dy
            }
        } ?: balloons.firstOrNull()

        if (target != null) {
            val px = target.xFraction * canvasWidth
            val py = target.yFraction * canvasHeight
            balloons.remove(target)
            balloons.add(spawnBalloon(startAtBottom = true))

            particles.burst(
                x = px,
                y = py,
                count = if (target.isStar) 38 else 26,
                baseColor = target.color,
                shape = if (target.isStar) ParticleShape.STAR else ParticleShape.CONFETTI
            )

            popCount++
            if (target.isStar || popCount % 5 == 0) {
                soundEngine.play(SoundEffect.CHIME)
                sticker = SurpriseSticker(
                    xFraction = target.xFraction.coerceIn(0.2f, 0.8f),
                    yFraction = target.yFraction.coerceIn(0.25f, 0.7f),
                    life = 1.6f,
                    kind = popCount % 3
                )
                voiceSpeaker.speak(if (target.isStar) "Yay! Star!" else "Pop!")
            } else {
                soundEngine.play(SoundEffect.POP, rate = 0.85f + Random.nextFloat() * 0.35f)
            }
        }
    }

    // 60fps animation loop
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var elapsed = 0f
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                elapsed += dt

                for (i in balloons.indices) {
                    val b = balloons[i]
                    b.yFraction -= b.speed * dt
                    b.xFraction = (b.xFraction + sin(elapsed * 2.1f + b.wobblePhase) * 0.018f * dt)
                        .coerceIn(0.08f, 0.92f)
                    if (b.yFraction < -0.18f) {
                        balloons[i] = spawnBalloon(startAtBottom = true)
                    }
                }
                particles.update(dt)
                sticker?.let { s ->
                    s.life -= dt
                    s.yFraction -= 0.05f * dt
                    if (s.life <= 0f) sticker = null
                }
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC), Color(0xFFE1F5FE))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read frameTick so Compose invalidates every frame
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Soft sun & clouds
            drawCircle(Color(0xFFFFF176), radius = size.height * 0.1f, center = Offset(size.width * 0.88f, size.height * 0.14f))

            // Draw floating balloons
            for (b in balloons) {
                val cx = b.xFraction * size.width
                val cy = b.yFraction * size.height
                val r = b.radiusDp * (size.height / 720f)

                if (b.isStar) {
                    ParticleSystem.drawStar(this, Offset(cx, cy), r * 1.05f, Color(0xFFFFD600))
                    ParticleSystem.drawStar(this, Offset(cx, cy), r * 0.65f, Color(0xFFFFF59D))
                } else {
                    // Curly string
                    val stringPath = Path().apply {
                        moveTo(cx, cy + r * 1.15f)
                        quadraticTo(cx - r * 0.25f, cy + r * 1.65f, cx + r * 0.1f, cy + r * 2.15f)
                    }
                    drawPath(stringPath, color = Color.White.copy(alpha = 0.75f), style = Stroke(width = 4f))

                    // Balloon oval
                    drawOval(
                        color = b.color,
                        topLeft = Offset(cx - r * 0.88f, cy - r * 1.12f),
                        size = Size(r * 1.76f, r * 2.24f)
                    )
                    // Specular highlight
                    drawOval(
                        color = Color.White.copy(alpha = 0.35f),
                        topLeft = Offset(cx - r * 0.52f, cy - r * 0.78f),
                        size = Size(r * 0.36f, r * 0.68f)
                    )
                    // Friendly eyes & smile on balloon
                    drawCircle(Color(0xFF263238), radius = r * 0.08f, center = Offset(cx - r * 0.24f, cy - r * 0.1f))
                    drawCircle(Color(0xFF263238), radius = r * 0.08f, center = Offset(cx + r * 0.24f, cy - r * 0.1f))
                    drawArc(
                        color = Color(0xFF263238),
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(cx - r * 0.25f, cy + r * 0.05f),
                        size = Size(r * 0.5f, r * 0.28f),
                        style = Stroke(width = 5f)
                    )
                }
            }

            // Surprise sticker
            sticker?.let { s ->
                val sx = s.xFraction * size.width
                val sy = s.yFraction * size.height
                val alpha = (s.life / 1.6f).coerceIn(0f, 1f)
                val sr = 84f * (size.height / 720f)
                when (s.kind) {
                    0 -> {
                        drawCircle(Color(0xFFFFD600).copy(alpha = alpha), sr, Offset(sx, sy))
                        drawCircle(Color(0xFFFFF59D).copy(alpha = alpha), sr * 0.72f, Offset(sx, sy))
                    }
                    1 -> {
                        val rainbow = listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF66BB6A), Color(0xFF42A5F5))
                        rainbow.forEachIndexed { idx, col ->
                            val rad = sr * (1.25f - idx * 0.18f)
                            drawArc(
                                color = col.copy(alpha = alpha),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(sx - rad, sy - rad),
                                size = Size(rad * 2f, rad * 2f),
                                style = Stroke(width = 16f)
                            )
                        }
                    }
                    else -> {
                        ParticleSystem.drawStar(this, Offset(sx, sy), sr * 1.1f, Color(0xFFFFD600).copy(alpha = alpha))
                    }
                }
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }
    }
}
