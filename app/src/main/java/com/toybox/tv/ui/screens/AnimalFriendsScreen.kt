package com.toybox.tv.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

enum class AnimalFriend(
    val displayName: String,
    val soundPhrase: String,
    val bgTop: Color,
    val bgBottom: Color,
    val primaryColor: Color,
    val pitchRate: Float
) {
    LION("Lion", "Lion! Roar!", Color(0xFFFFE082), Color(0xFFFFB74D), Color(0xFFFFA726), 0.78f),
    ELEPHANT("Elephant", "Elephant! Toot toot!", Color(0xFFB3E5FC), Color(0xFF4FC3F7), Color(0xFF90A4AE), 0.72f),
    DUCK("Duck", "Duck! Quack quack!", Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFFFFEB3B), 1.32f),
    COW("Cow", "Cow! Moo!", Color(0xFFC8E6C9), Color(0xFF81C784), Color(0xFFFFFFFF), 0.82f),
    DOG("Puppy", "Puppy! Woof woof!", Color(0xFFFFCCBC), Color(0xFFFFAB91), Color(0xFFBCAAA4), 1.08f),
    FROG("Frog", "Frog! Ribbit ribbit!", Color(0xFFDCEDC8), Color(0xFF9CCC65), Color(0xFF66BB6A), 1.25f),
    CAT("Kitty", "Kitty! Meow!", Color(0xFFF8BBD0), Color(0xFFF48FB1), Color(0xFFFFCC80), 1.38f),
    OWL("Owl", "Owl! Hoo hoo!", Color(0xFFD1C4E9), Color(0xFF9575CD), Color(0xFF8D6E63), 0.94f)
}

@Composable
fun AnimalFriendsScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val animals = AnimalFriend.entries
    var currentIndex by remember { mutableIntStateOf(0) }
    val bounce = remember { Animatable(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val animal = animals[currentIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(animal.soundPhrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> {
                currentIndex = (currentIndex - 1 + animals.size) % animals.size
            }
            ToyboxKey.RIGHT -> {
                currentIndex = (currentIndex + 1) % animals.size
            }
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> {
                // Bounce & dance the current animal
            }
        }
        val active = animals[currentIndex]
        val realSfx = when (active) {
            AnimalFriend.LION -> SoundEffect.REAL_LION
            AnimalFriend.ELEPHANT -> SoundEffect.REAL_ELEPHANT
            AnimalFriend.DUCK -> SoundEffect.REAL_DUCK
            AnimalFriend.COW -> SoundEffect.REAL_COW
            AnimalFriend.DOG -> SoundEffect.REAL_DOG
            AnimalFriend.FROG -> SoundEffect.REAL_BOING
            AnimalFriend.CAT -> SoundEffect.REAL_CAT
            AnimalFriend.OWL -> SoundEffect.REAL_OWL
        }
        soundEngine.play(realSfx)
        voiceSpeaker.speak(active.displayName)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.45f,
            count = 30,
            baseColor = active.primaryColor,
            shape = ParticleShape.STAR
        )
        launch {
            bounce.snapTo(1f)
            bounce.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow)
            )
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
            .background(Brush.verticalGradient(listOf(animal.bgTop, animal.bgBottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val cx = size.width * 0.5f
            val cy = size.height * 0.48f - bounce.value * size.height * 0.11f
            val r = minOf(size.width, size.height) * 0.28f * (1f + bounce.value * 0.12f)

            // Soft ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(cx - r * 0.9f, size.height * 0.78f),
                size = Size(r * 1.8f, r * 0.32f)
            )

            drawAnimal(animal, Offset(cx, cy), r)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TvOverscan.Vertical + 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = animal.displayName.uppercase(),
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 3.sp
            )
        }
    }
}

private fun DrawScope.drawAnimal(animal: AnimalFriend, c: Offset, r: Float) {
    when (animal) {
        AnimalFriend.LION -> {
            // Fluffy mane
            for (i in 0 until 12) {
                val angle = i * 30.0 * Math.PI / 180.0
                val mx = c.x + (kotlin.math.cos(angle) * r * 0.85f).toFloat()
                val my = c.y + (kotlin.math.sin(angle) * r * 0.85f).toFloat()
                drawCircle(Color(0xFFE65100), r * 0.36f, Offset(mx, my))
            }
            // Ears
            drawCircle(Color(0xFFFFCA28), r * 0.25f, Offset(c.x - r * 0.65f, c.y - r * 0.65f))
            drawCircle(Color(0xFFFFCA28), r * 0.25f, Offset(c.x + r * 0.65f, c.y - r * 0.65f))
            // Face
            drawCircle(Color(0xFFFFCA28), r * 0.82f, c)
            drawCuteFace(c, r, noseColor = Color(0xFF5D4037))
        }
        AnimalFriend.ELEPHANT -> {
            // Giant floppy ears
            drawOval(Color(0xFF78909C), Offset(c.x - r * 1.5f, c.y - r * 0.65f), Size(r * 0.95f, r * 1.25f))
            drawOval(Color(0xFF78909C), Offset(c.x + r * 0.55f, c.y - r * 0.65f), Size(r * 0.95f, r * 1.25f))
            drawOval(Color(0xFFF8BBD0), Offset(c.x - r * 1.32f, c.y - r * 0.45f), Size(r * 0.58f, r * 0.85f))
            drawOval(Color(0xFFF8BBD0), Offset(c.x + r * 0.74f, c.y - r * 0.45f), Size(r * 0.58f, r * 0.85f))
            // Head
            drawCircle(Color(0xFF90A4AE), r * 0.84f, c)
            // Trunk
            drawOval(Color(0xFF78909C), Offset(c.x - r * 0.2f, c.y + r * 0.1f), Size(r * 0.4f, r * 0.95f))
            drawCuteEyes(c, r)
        }
        AnimalFriend.DUCK -> {
            drawCircle(Color(0xFFFFEB3B), r * 0.86f, c)
            // Tuft
            drawCircle(Color(0xFFFFD600), r * 0.22f, Offset(c.x, c.y - r * 0.88f))
            // Orange bill
            drawOval(Color(0xFFFF7043), Offset(c.x - r * 0.36f, c.y + r * 0.05f), Size(r * 0.72f, r * 0.42f))
            drawCuteEyes(c, r)
        }
        AnimalFriend.COW -> {
            // Horns & ears
            drawOval(Color(0xFFFFE082), Offset(c.x - r * 0.62f, c.y - r * 1.02f), Size(r * 0.24f, r * 0.45f))
            drawOval(Color(0xFFFFE082), Offset(c.x + r * 0.38f, c.y - r * 1.02f), Size(r * 0.24f, r * 0.45f))
            drawOval(Color(0xFF424242), Offset(c.x - r * 1.1f, c.y - r * 0.5f), Size(r * 0.5f, r * 0.3f))
            drawOval(Color(0xFF424242), Offset(c.x + r * 0.6f, c.y - r * 0.5f), Size(r * 0.5f, r * 0.3f))
            // White face + spot
            drawCircle(Color.White, r * 0.84f, c)
            drawCircle(Color(0xFF37474F), r * 0.32f, Offset(c.x - r * 0.38f, c.y - r * 0.35f))
            // Pink muzzle
            drawOval(Color(0xFFF8BBD0), Offset(c.x - r * 0.55f, c.y + r * 0.1f), Size(r * 1.1f, r * 0.62f))
            drawCircle(Color(0xFFAD1457), r * 0.08f, Offset(c.x - r * 0.2f, c.y + r * 0.34f))
            drawCircle(Color(0xFFAD1457), r * 0.08f, Offset(c.x + r * 0.2f, c.y + r * 0.34f))
            drawCuteEyes(c, r)
        }
        AnimalFriend.DOG -> {
            // Floppy ears
            drawOval(Color(0xFF795548), Offset(c.x - r * 1.12f, c.y - r * 0.35f), Size(r * 0.5f, r * 1.0f))
            drawOval(Color(0xFF795548), Offset(c.x + r * 0.62f, c.y - r * 0.35f), Size(r * 0.5f, r * 1.0f))
            drawCircle(Color(0xFFD7CCC8), r * 0.84f, c)
            // Happy pink tongue
            drawOval(Color(0xFFFF80AB), Offset(c.x - r * 0.15f, c.y + r * 0.32f), Size(r * 0.3f, r * 0.36f))
            drawCuteFace(c, r, noseColor = Color(0xFF3E2723))
        }
        AnimalFriend.FROG -> {
            // Big top frog eyes
            drawCircle(Color(0xFF66BB6A), r * 0.36f, Offset(c.x - r * 0.48f, c.y - r * 0.68f))
            drawCircle(Color(0xFF66BB6A), r * 0.36f, Offset(c.x + r * 0.48f, c.y - r * 0.68f))
            drawCircle(Color.White, r * 0.24f, Offset(c.x - r * 0.48f, c.y - r * 0.68f))
            drawCircle(Color.White, r * 0.24f, Offset(c.x + r * 0.48f, c.y - r * 0.68f))
            drawCircle(Color(0xFF212121), r * 0.12f, Offset(c.x - r * 0.48f, c.y - r * 0.68f))
            drawCircle(Color(0xFF212121), r * 0.12f, Offset(c.x + r * 0.48f, c.y - r * 0.68f))
            // Wide happy face
            drawOval(Color(0xFF66BB6A), Offset(c.x - r * 0.95f, c.y - r * 0.65f), Size(r * 1.9f, r * 1.45f))
            // Rosy cheeks & giant smile
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.6f), r * 0.16f, Offset(c.x - r * 0.58f, c.y + r * 0.1f))
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.6f), r * 0.16f, Offset(c.x + r * 0.58f, c.y + r * 0.1f))
            drawArc(
                color = Color(0xFF1B5E20),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(c.x - r * 0.55f, c.y - r * 0.1f),
                size = Size(r * 1.1f, r * 0.55f),
                style = Stroke(width = 10f)
            )
        }
        AnimalFriend.CAT -> {
            // Pointy ears
            val leftEar = Path().apply {
                moveTo(c.x - r * 0.72f, c.y - r * 0.25f)
                lineTo(c.x - r * 0.62f, c.y - r * 1.05f)
                lineTo(c.x - r * 0.12f, c.y - r * 0.68f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(c.x + r * 0.72f, c.y - r * 0.25f)
                lineTo(c.x + r * 0.62f, c.y - r * 1.05f)
                lineTo(c.x + r * 0.12f, c.y - r * 0.68f)
                close()
            }
            drawPath(leftEar, Color(0xFFFFB74D))
            drawPath(rightEar, Color(0xFFFFB74D))
            drawCircle(Color(0xFFFFCC80), r * 0.84f, c)
            // Whiskers
            val wCol = Color(0xFF5D4037)
            drawLine(wCol, Offset(c.x - r * 0.45f, c.y + r * 0.12f), Offset(c.x - r * 1.05f, c.y + r * 0.02f), 6f)
            drawLine(wCol, Offset(c.x - r * 0.45f, c.y + r * 0.24f), Offset(c.x - r * 1.05f, c.y + r * 0.30f), 6f)
            drawLine(wCol, Offset(c.x + r * 0.45f, c.y + r * 0.12f), Offset(c.x + r * 1.05f, c.y + r * 0.02f), 6f)
            drawLine(wCol, Offset(c.x + r * 0.45f, c.y + r * 0.24f), Offset(c.x + r * 1.05f, c.y + r * 0.30f), 6f)
            drawCuteFace(c, r, noseColor = Color(0xFFF06292))
        }
        AnimalFriend.OWL -> {
            drawOval(Color(0xFF8D6E63), Offset(c.x - r * 0.82f, c.y - r * 0.9f), Size(r * 1.64f, r * 1.85f))
            // Cream belly
            drawOval(Color(0xFFFFF8E1), Offset(c.x - r * 0.52f, c.y + r * 0.12f), Size(r * 1.04f, r * 0.72f))
            // Big round owl eyes
            drawCircle(Color.White, r * 0.36f, Offset(c.x - r * 0.34f, c.y - r * 0.22f))
            drawCircle(Color.White, r * 0.36f, Offset(c.x + r * 0.34f, c.y - r * 0.22f))
            drawCircle(Color(0xFF263238), r * 0.18f, Offset(c.x - r * 0.34f, c.y - r * 0.22f))
            drawCircle(Color(0xFF263238), r * 0.18f, Offset(c.x + r * 0.34f, c.y - r * 0.22f))
            // Yellow beak
            val beak = Path().apply {
                moveTo(c.x - r * 0.16f, c.y + r * 0.02f)
                lineTo(c.x + r * 0.16f, c.y + r * 0.02f)
                lineTo(c.x, c.y + r * 0.28f)
                close()
            }
            drawPath(beak, Color(0xFFFFCA28))
        }
    }
}

private fun DrawScope.drawCuteEyes(c: Offset, r: Float) {
    drawCircle(Color.White, r * 0.18f, Offset(c.x - r * 0.30f, c.y - r * 0.18f))
    drawCircle(Color.White, r * 0.18f, Offset(c.x + r * 0.30f, c.y - r * 0.18f))
    drawCircle(Color(0xFF212121), r * 0.11f, Offset(c.x - r * 0.30f, c.y - r * 0.18f))
    drawCircle(Color(0xFF212121), r * 0.11f, Offset(c.x + r * 0.30f, c.y - r * 0.18f))
    drawCircle(Color.White, r * 0.04f, Offset(c.x - r * 0.34f, c.y - r * 0.22f))
    drawCircle(Color.White, r * 0.04f, Offset(c.x + r * 0.26f, c.y - r * 0.22f))
}

private fun DrawScope.drawCuteFace(c: Offset, r: Float, noseColor: Color) {
    drawCuteEyes(c, r)
    drawCircle(noseColor, r * 0.12f, Offset(c.x, c.y + r * 0.08f))
    drawArc(
        color = Color(0xFF3E2723),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(c.x - r * 0.26f, c.y + r * 0.12f),
        size = Size(r * 0.52f, r * 0.28f),
        style = Stroke(width = 8f)
    )
}
