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

private enum class ToyShape(val label: String) {
    CIRCLE("Circle"),
    STAR("Star"),
    HEART("Heart"),
    TRIANGLE("Triangle"),
    SQUARE("Square"),
    DIAMOND("Diamond")
}

private data class NamedColor(val name: String, val color: Color)

@Composable
fun ColorsShapesScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val shapes = ToyShape.entries
    val colors = remember {
        listOf(
            NamedColor("Red", Color(0xFFFF5252)),
            NamedColor("Yellow", Color(0xFFFFD600)),
            NamedColor("Blue", Color(0xFF42A5F5)),
            NamedColor("Green", Color(0xFF66BB6A)),
            NamedColor("Orange", Color(0xFFFF9800)),
            NamedColor("Pink", Color(0xFFFF80AB)),
            NamedColor("Purple", Color(0xFFAB47BC))
        )
    }

    var shapeIndex by remember { mutableIntStateOf(0) }
    var colorIndex by remember { mutableIntStateOf(0) }
    val bounce = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val shape = shapes[shapeIndex]
    val namedColor = colors[colorIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak("${namedColor.name} ${shape.label}")
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> shapeIndex = (shapeIndex - 1 + shapes.size) % shapes.size
            ToyboxKey.RIGHT -> shapeIndex = (shapeIndex + 1) % shapes.size
            ToyboxKey.UP -> colorIndex = (colorIndex + 1) % colors.size
            ToyboxKey.DOWN -> colorIndex = (colorIndex - 1 + colors.size) % colors.size
            ToyboxKey.OK -> {
                shapeIndex = (shapeIndex + 1) % shapes.size
                colorIndex = (colorIndex + 1) % colors.size
            }
        }
        val activeShape = shapes[shapeIndex]
        val activeColor = colors[colorIndex]
        soundEngine.play(SoundEffect.CHIME, rate = 0.9f + (colorIndex % 4) * 0.1f)
        voiceSpeaker.speak("${activeColor.name} ${activeShape.label}!")
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.5f,
            count = 32,
            baseColor = activeColor.color,
            shape = ParticleShape.CONFETTI
        )
        launch {
            bounce.snapTo(1f)
            bounce.animateTo(0f, spring(dampingRatio = 0.36f, stiffness = Spring.StiffnessMedium))
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
                    listOf(Color(0xFFFFFDE7), Color(0xFFFFF8E1), Color(0xFFE1F5FE))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.50f - bounce.value * size.height * 0.08f
            val r = minOf(size.width, size.height) * 0.27f * (1f + bounce.value * 0.14f)

            drawBigShape(shape, Offset(cx, cy), r, namedColor.color)

            // Cute smiling face in center of every shape
            drawCircle(Color.White, r * 0.14f, Offset(cx - r * 0.24f, cy - r * 0.1f))
            drawCircle(Color.White, r * 0.14f, Offset(cx + r * 0.24f, cy - r * 0.1f))
            drawCircle(Color(0xFF212121), r * 0.08f, Offset(cx - r * 0.24f, cy - r * 0.1f))
            drawCircle(Color(0xFF212121), r * 0.08f, Offset(cx + r * 0.24f, cy - r * 0.1f))
            drawArc(
                color = Color(0xFF212121),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.25f, cy + r * 0.06f),
                size = Size(r * 0.5f, r * 0.26f),
                style = Stroke(width = 8f)
            )

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = "${namedColor.name.uppercase()} ${shape.label.uppercase()}",
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = namedColor.color,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}

private fun DrawScope.drawBigShape(shape: ToyShape, c: Offset, r: Float, color: Color) {
    when (shape) {
        ToyShape.CIRCLE -> drawCircle(color, r, c)
        ToyShape.STAR -> ParticleSystem.drawStar(this, c, r * 1.15f, color)
        ToyShape.SQUARE -> drawRoundRect(
            color = color,
            topLeft = Offset(c.x - r * 0.9f, c.y - r * 0.9f),
            size = Size(r * 1.8f, r * 1.8f),
            cornerRadius = CornerRadius(32f, 32f)
        )
        ToyShape.TRIANGLE -> {
            val path = Path().apply {
                moveTo(c.x, c.y - r * 1.05f)
                lineTo(c.x + r * 1.05f, c.y + r * 0.85f)
                lineTo(c.x - r * 1.05f, c.y + r * 0.85f)
                close()
            }
            drawPath(path, color)
        }
        ToyShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(c.x, c.y - r * 1.12f)
                lineTo(c.x + r * 0.92f, c.y)
                lineTo(c.x, c.y + r * 1.12f)
                lineTo(c.x - r * 0.92f, c.y)
                close()
            }
            drawPath(path, color)
        }
        ToyShape.HEART -> {
            val path = Path().apply {
                moveTo(c.x, c.y + r * 0.95f)
                cubicTo(
                    c.x - r * 1.45f, c.y + r * 0.15f,
                    c.x - r * 1.05f, c.y - r * 1.1f,
                    c.x, c.y - r * 0.32f
                )
                cubicTo(
                    c.x + r * 1.05f, c.y - r * 1.1f,
                    c.x + r * 1.45f, c.y + r * 0.15f,
                    c.x, c.y + r * 0.95f
                )
                close()
            }
            drawPath(path, color)
        }
    }
}
