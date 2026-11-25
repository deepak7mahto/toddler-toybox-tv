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
import androidx.compose.ui.geometry.Offset
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

data class AlphabetCard(
    val letter: String,
    val word: String,
    val spoken: String
)

object AlphabetDeck {
    val all = listOf(
        AlphabetCard("A", "Apple", "A for Apple"),
        AlphabetCard("B", "Ball", "B for Ball"),
        AlphabetCard("C", "Cat", "C for Cat"),
        AlphabetCard("D", "Duck", "D for Duck"),
        AlphabetCard("E", "Elephant", "E for Elephant"),
        AlphabetCard("F", "Fish", "F for Fish"),
        AlphabetCard("G", "Grapes", "G for Grapes"),
        AlphabetCard("H", "Hat", "H for Hat"),
        AlphabetCard("I", "Ice Cream", "I for Ice Cream"),
        AlphabetCard("J", "Jump", "J for Jump"),
        AlphabetCard("K", "Kite", "K for Kite"),
        AlphabetCard("L", "Lion", "L for Lion"),
        AlphabetCard("M", "Moon", "M for Moon"),
        AlphabetCard("N", "Nest", "N for Nest"),
        AlphabetCard("O", "Owl", "O for Owl"),
        AlphabetCard("P", "Puppy", "P for Puppy"),
        AlphabetCard("Q", "Queen", "Q for Queen"),
        AlphabetCard("R", "Rainbow", "R for Rainbow"),
        AlphabetCard("S", "Sun", "S for Sun"),
        AlphabetCard("T", "Train", "T for Train"),
        AlphabetCard("U", "Umbrella", "U for Umbrella"),
        AlphabetCard("V", "Van", "V for Van"),
        AlphabetCard("W", "Whale", "W for Whale"),
        AlphabetCard("X", "Xylophone", "X for Xylophone"),
        AlphabetCard("Y", "Yo-Yo", "Y for Yo-Yo"),
        AlphabetCard("Z", "Zebra", "Z for Zebra")
    )
}

@Composable
fun AbcScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val accentColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFF9800),
            Color(0xFF42A5F5),
            Color(0xFF66BB6A),
            Color(0xFFAB47BC),
            Color(0xFF26A69A)
        )
    }

    val deck = AlphabetDeck.all
    var index by remember { mutableIntStateOf(0) }
    val popScale = remember { Animatable(1f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val card = deck[index]
    val accent = accentColors[index % accentColors.size]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(card.spoken)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        when (lastKeyEvent.second) {
            ToyboxKey.LEFT -> index = (index - 1 + deck.size) % deck.size
            ToyboxKey.RIGHT, ToyboxKey.UP, ToyboxKey.OK -> index = (index + 1) % deck.size
            ToyboxKey.DOWN -> {
                // Repeat & bounce the current letter
            }
        }
        val activeCard = deck[index]
        soundEngine.play(SoundEffect.CHIME, rate = 0.92f + (index % 5) * 0.06f)
        voiceSpeaker.speak(activeCard.spoken)
        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.46f,
            count = 32,
            baseColor = accentColors[index % accentColors.size],
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
                    listOf(Color(0xFF42A5F5), Color(0xFF81D4FA), Color(0xFFE1F5FE))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            drawCircle(
                color = Color.White,
                radius = minOf(size.width, size.height) * 0.30f * popScale.value,
                center = Offset(size.width * 0.5f, size.height * 0.46f)
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
                text = "A TO Z  (${index + 1} / 26)",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "${card.letter} ${card.letter.lowercase()}",
                fontSize = 165.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                modifier = Modifier.scale(popScale.value)
            )

            Text(
                text = "${card.letter} FOR ${card.word.uppercase()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A237E),
                letterSpacing = 2.sp
            )
        }
    }
}
