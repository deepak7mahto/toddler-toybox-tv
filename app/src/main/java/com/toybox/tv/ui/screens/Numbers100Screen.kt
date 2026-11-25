package com.toybox.tv.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
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

object NumberWords {
    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun toWords(n: Int): String {
        val clamped = n.coerceIn(1, 100)
        return when {
            clamped == 100 -> "One Hundred"
            clamped < 20 -> ones[clamped]
            clamped % 10 == 0 -> tens[clamped / 10]
            else -> "${tens[clamped / 10]} ${ones[clamped % 10]}"
        }
    }
}

@Composable
fun Numbers100Screen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val accentColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFF9800),
            Color(0xFF43A047),
            Color(0xFF1E88E5),
            Color(0xFFAB47BC),
            Color(0xFF00897B)
        )
    }

    var number by remember { mutableIntStateOf(1) }
    val popScale = remember { Animatable(1f) }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val word = NumberWords.toWords(number)
    val accent = accentColors[(number - 1) % accentColors.size]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(word)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> number = if (number > 1) number - 1 else 100
            ToyboxKey.RIGHT, ToyboxKey.OK -> number = if (number < 100) number + 1 else 1
            ToyboxKey.UP -> number = if (number + 10 <= 100) number + 10 else 100
            ToyboxKey.DOWN -> {
                // Repeat & bounce current number
            }
        }

        val activeWord = NumberWords.toWords(number)
        val isMilestone = number % 10 == 0
        if (isMilestone) {
            soundEngine.play(SoundEffect.FIREWORK_BOOM)
            soundEngine.play(SoundEffect.CHIME)
            voiceSpeaker.speak(if (number == 100) "One Hundred! Super star!" else "$activeWord! Yay!")
        } else {
            soundEngine.play(SoundEffect.CHIME, rate = 0.90f + (number % 6) * 0.05f)
            voiceSpeaker.speak(activeWord)
        }

        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.45f,
            count = if (isMilestone) 50 else 26,
            baseColor = accentColors[(number - 1) % accentColors.size],
            speed = if (isMilestone) 580f else 420f,
            shape = ParticleShape.STAR
        )
        launch {
            popScale.snapTo(1.28f)
            popScale.animateTo(1f, spring(dampingRatio = 0.38f, stiffness = Spring.StiffnessMedium))
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
                    listOf(Color(0xFF26A69A), Color(0xFF80CBC4), Color(0xFFE0F2F1))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            // Giant white circle behind the number
            drawCircle(
                color = Color.White,
                radius = minOf(size.width, size.height) * 0.29f * popScale.value,
                center = Offset(size.width * 0.5f, size.height * 0.44f)
            )

            // Visual counting stars:
            // 1..10 shows exact stars; 11..100 shows big gold tens-stars + bright cyan ones-stars
            val tensCount = number / 10
            val onesCount = number % 10
            val starY = size.height * 0.78f

            if (number <= 10) {
                val spacing = (size.width * 0.70f) / number.coerceAtLeast(1)
                val startX = size.width * 0.5f - spacing * (number - 1) / 2f
                for (i in 0 until number) {
                    ParticleSystem.drawStar(
                        this,
                        Offset(startX + i * spacing, starY),
                        outerRadius = 32f,
                        color = Color(0xFFFFD600)
                    )
                }
            } else {
                val totalIcons = tensCount + onesCount
                val spacing = (size.width * 0.78f) / totalIcons.coerceAtLeast(1)
                val startX = size.width * 0.5f - spacing * (totalIcons - 1) / 2f
                for (i in 0 until tensCount) {
                    ParticleSystem.drawStar(
                        this,
                        Offset(startX + i * spacing, starY),
                        outerRadius = 30f,
                        color = Color(0xFFFFD600)
                    )
                }
                for (j in 0 until onesCount) {
                    ParticleSystem.drawStar(
                        this,
                        Offset(startX + (tensCount + j) * spacing, starY),
                        outerRadius = 19f,
                        color = Color(0xFFFF80AB)
                    )
                }
            }

            // Cheerful 1..100 progress bar at bottom
            val barW = size.width * 0.76f
            val barLeft = (size.width - barW) / 2f
            val barTop = size.height * 0.855f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(barLeft, barTop),
                size = Size(barW, 18f),
                cornerRadius = CornerRadius(9f, 9f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(barLeft, barTop),
                size = Size(barW * (number / 100f), 18f),
                cornerRadius = CornerRadius(9f, 9f)
            )

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TvOverscan.Horizontal, vertical = TvOverscan.Vertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "COUNT 1 TO 100   (UP = +10)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "$number",
                fontSize = if (number == 100) 148.sp else 178.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                modifier = Modifier.scale(popScale.value)
            )

            Text(
                text = word.uppercase(),
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF004D40),
                letterSpacing = 2.sp
            )
        }
    }
}
