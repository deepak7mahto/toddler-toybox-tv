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

data class NurserySong(
    val title: String,
    val notes: List<Int> // 0=C5, 1=D5, 2=E5, 3=F5, 4=G5, 5=A5, 6=B5, 7=C6
)

object NurserySongs {
    val scale = listOf(
        SoundEffect.NOTE_C5,
        SoundEffect.NOTE_D5,
        SoundEffect.NOTE_E5,
        SoundEffect.NOTE_F5,
        SoundEffect.NOTE_G5,
        SoundEffect.NOTE_A5,
        SoundEffect.NOTE_B5,
        SoundEffect.NOTE_C6
    )

    val all = listOf(
        NurserySong(
            title = "Twinkle Twinkle",
            notes = listOf(0, 0, 4, 4, 5, 5, 4, 3, 3, 2, 2, 1, 1, 0)
        ),
        NurserySong(
            title = "Happy Birthday",
            notes = listOf(0, 0, 1, 0, 3, 2, 0, 0, 1, 0, 4, 3, 0, 0, 7, 5, 3, 2, 1)
        ),
        NurserySong(
            title = "Mary Had a Little Lamb",
            notes = listOf(2, 1, 0, 1, 2, 2, 2, 1, 1, 1, 2, 4, 4, 2, 1, 0, 1, 2, 2, 2, 1, 1, 2, 1, 0)
        ),
        NurserySong(
            title = "Row Your Boat",
            notes = listOf(0, 0, 0, 1, 2, 2, 1, 2, 3, 4, 7, 7, 7, 4, 4, 4, 2, 2, 2, 0, 0, 0, 4, 3, 2, 1, 0)
        )
    )
}

@Composable
fun NurseryPianoScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val keyColors = remember {
        listOf(
            Color(0xFFFF5252),
            Color(0xFFFF9800),
            Color(0xFFFFD600),
            Color(0xFF66BB6A),
            Color(0xFF26C6DA),
            Color(0xFF42A5F5),
            Color(0xFFAB47BC),
            Color(0xFFFF80AB)
        )
    }

    var songIndex by remember { mutableIntStateOf(0) }
    var noteStep by remember { mutableIntStateOf(0) }
    val keyPresses = remember { List(8) { Animatable(0f) } }
    val particles = remember { ParticleSystem(maxParticles = 240) }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val song = NurserySongs.all[songIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(song.title)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val key = lastKeyEvent.second
        if (key == ToyboxKey.LEFT) {
            songIndex = (songIndex - 1 + NurserySongs.all.size) % NurserySongs.all.size
            noteStep = 0
            soundEngine.play(SoundEffect.CHIME)
            voiceSpeaker.speak(NurserySongs.all[songIndex].title)
            return@LaunchedEffect
        }
        if (key == ToyboxKey.RIGHT) {
            songIndex = (songIndex + 1) % NurserySongs.all.size
            noteStep = 0
            soundEngine.play(SoundEffect.CHIME)
            voiceSpeaker.speak(NurserySongs.all[songIndex].title)
            return@LaunchedEffect
        }

        // OK / UP / DOWN plays the next note in the nursery rhyme!
        val currentSong = NurserySongs.all[songIndex]
        val noteIdx = currentSong.notes[noteStep % currentSong.notes.size]
        noteStep++
        soundEngine.play(NurserySongs.scale[noteIdx])

        val keyX = canvasWidth * (0.10f + noteIdx * 0.114f)
        particles.burst(
            x = keyX,
            y = canvasHeight * 0.42f,
            count = 32,
            baseColor = keyColors[noteIdx],
            speed = 500f,
            shape = ParticleShape.STAR
        )

        launch {
            keyPresses[noteIdx].snapTo(1f)
            keyPresses[noteIdx].animateTo(
                0f,
                spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF311B92), Color(0xFF512DA8), Color(0xFF7E57C2))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val nextNoteIdx = song.notes[noteStep % song.notes.size]
            val keyWidth = size.width * 0.095f
            val keyHeight = size.height * 0.48f
            val baseTop = size.height * 0.42f

            for (i in 0 until 8) {
                val press = keyPresses[i].value
                val cx = size.width * (0.10f + i * 0.114f)
                val left = cx - keyWidth / 2f
                val top = baseTop + press * 28f
                val isNext = i == nextNoteIdx

                if (isNext || press > 0.05f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = if (press > 0.05f) 0.65f else 0.35f),
                        topLeft = Offset(left - 10f, top - 10f),
                        size = Size(keyWidth + 20f, keyHeight + 20f),
                        cornerRadius = CornerRadius(32f, 32f)
                    )
                }

                drawRoundRect(
                    color = keyColors[i],
                    topLeft = Offset(left, top),
                    size = Size(keyWidth, keyHeight),
                    cornerRadius = CornerRadius(26f, 26f)
                )

                // Cute star hint on the next note to play
                if (isNext) {
                    ParticleSystem.drawStar(
                        this,
                        Offset(cx, top - 44f),
                        outerRadius = 26f,
                        color = Color(0xFFFFEE58)
                    )
                }
            }

            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = song.title.uppercase(),
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFEE58),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 12.dp)
        )
    }
}
