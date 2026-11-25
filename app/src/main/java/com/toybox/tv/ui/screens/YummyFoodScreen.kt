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

private enum class YummyTreat(
    val displayName: String,
    val phrase: String,
    val color: Color,
    val accent: Color
) {
    APPLE("Apple", "Apple! Crunch! Yummy!", Color(0xFFFF5252), Color(0xFF66BB6A)),
    MANGO("Mango", "Mango! Sweet and yummy!", Color(0xFFFFB300), Color(0xFF66BB6A)),
    WATERMELON("Watermelon", "Watermelon! Juicy crunch!", Color(0xFFFF5252), Color(0xFF43A047)),
    ICE_CREAM("Ice Cream", "Ice Cream! Yummy!", Color(0xFFFF80AB), Color(0xFFFFCC80)),
    CUPCAKE("Cupcake", "Cupcake! Sprinkles! Yummy!", Color(0xFFAB47BC), Color(0xFFFFCA28)),
    DONUT("Donut", "Donut! Yum yum!", Color(0xFFF06292), Color(0xFFFFEE58))
}

@Composable
fun YummyFoodScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val treats = YummyTreat.entries
    var idx by remember { mutableIntStateOf(0) }
    val chomp = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val treat = treats[idx]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(treat.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> idx = (idx - 1 + treats.size) % treats.size
            ToyboxKey.RIGHT -> idx = (idx + 1) % treats.size
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> Unit
        }
        val active = treats[idx]
        soundEngine.play(SoundEffect.CRUNCH, rate = 0.9f + (idx % 3) * 0.12f)
        voiceSpeaker.speak(active.phrase)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.48f,
            count = 30,
            baseColor = active.color,
            shape = ParticleShape.CONFETTI
        )
        launch {
            chomp.snapTo(1f)
            chomp.animateTo(0f, spring(dampingRatio = 0.36f, stiffness = Spring.StiffnessMedium))
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
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFCCBC))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f - chomp.value * 40f
            val r = minOf(size.width, size.height) * 0.25f * (1f + chomp.value * 0.14f)

            // Cute plate
            drawOval(Color.White, Offset(cx - r * 1.45f, size.height * 0.72f), Size(r * 2.9f, r * 0.52f))
            drawTreat(treat, Offset(cx, cy), r)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = treat.displayName.uppercase(),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFBF360C),
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp)
        )
    }
}

private fun DrawScope.drawTreat(t: YummyTreat, c: Offset, r: Float) {
    when (t) {
        YummyTreat.APPLE -> {
            drawCircle(t.color, r * 0.88f, c)
            drawOval(t.accent, Offset(c.x + r * 0.05f, c.y - r * 1.25f), Size(r * 0.52f, r * 0.34f))
        }
        YummyTreat.MANGO -> {
            drawOval(t.color, Offset(c.x - r * 0.78f, c.y - r * 0.96f), Size(r * 1.56f, r * 1.92f))
            drawOval(t.accent, Offset(c.x + r * 0.08f, c.y - r * 1.22f), Size(r * 0.48f, r * 0.32f))
        }
        YummyTreat.WATERMELON -> {
            drawArc(t.accent, 0f, 180f, true, Offset(c.x - r * 1.15f, c.y - r * 0.55f), Size(r * 2.3f, r * 1.85f))
            drawArc(t.color, 0f, 180f, true, Offset(c.x - r * 0.96f, c.y - r * 0.45f), Size(r * 1.92f, r * 1.55f))
            for (i in -2..2) {
                drawCircle(Color(0xFF212121), r * 0.06f, Offset(c.x + i * r * 0.28f, c.y + r * 0.55f))
            }
        }
        YummyTreat.ICE_CREAM -> {
            val cone = Path().apply {
                moveTo(c.x - r * 0.62f, c.y + r * 0.05f)
                lineTo(c.x + r * 0.62f, c.y + r * 0.05f)
                lineTo(c.x, c.y + r * 1.18f)
                close()
            }
            drawPath(cone, t.accent)
            drawCircle(t.color, r * 0.68f, Offset(c.x, c.y - r * 0.25f))
            drawCircle(Color(0xFFFF5252), r * 0.18f, Offset(c.x, c.y - r * 0.95f))
        }
        YummyTreat.CUPCAKE -> {
            drawArc(t.color, 180f, 180f, true, Offset(c.x - r * 0.85f, c.y - r * 0.78f), Size(r * 1.7f, r * 1.3f))
            drawRect(t.accent, Offset(c.x - r * 0.68f, c.y - r * 0.1f), Size(r * 1.36f, r * 0.92f))
            drawCircle(Color(0xFFFF5252), r * 0.18f, Offset(c.x, c.y - r * 0.82f))
        }
        YummyTreat.DONUT -> {
            drawCircle(Color(0xFFFFCC80), r * 0.95f, c)
            drawCircle(t.color, r * 0.82f, c)
            drawCircle(Color(0xFFFFE0B2), r * 0.34f, c)
            for (s in 0..5) {
                val a = s * 60.0 * Math.PI / 180.0
                drawCircle(t.accent, r * 0.08f, Offset(c.x + (kotlin.math.cos(a) * r * 0.58f).toFloat(), c.y + (kotlin.math.sin(a) * r * 0.58f).toFloat()))
            }
        }
    }
    // Cute eyes on every treat!
    drawCircle(Color(0xFF212121), r * 0.09f, Offset(c.x - r * 0.24f, c.y - r * 0.06f))
    drawCircle(Color(0xFF212121), r * 0.09f, Offset(c.x + r * 0.24f, c.y - r * 0.06f))
    drawArc(Color(0xFF212121), 20f, 140f, false, Offset(c.x - r * 0.22f, c.y + r * 0.06f), Size(r * 0.44f, r * 0.24f), style = Stroke(7f))
}
