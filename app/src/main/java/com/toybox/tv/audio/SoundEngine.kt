package com.toybox.tv.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

enum class SoundEffect {
    POP,
    PLOP,
    CHIME,
    PEEKABOO,
    FIREWORK_BOOM,
    DRUM_KICK,
    DRUM_SNARE,
    DRUM_TOM,
    DRUM_CYMBAL,
    WATER_SPLASH,
    CRUNCH,
    SPLAT,
    DINO_STOMP,
    SPARKLE,
    ANIMAL_BOING,
    HONK,
    SIREN,
    TRAIN,
    ROCKET,
    NOTE_C5,
    NOTE_D5,
    NOTE_E5,
    NOTE_F5,
    NOTE_G5,
    NOTE_A5,
    NOTE_B5,
    NOTE_C6
}

/**
 * Polyphonic, zero-asset SoundPool engine backed by procedurally synthesized WAV files.
 * Includes a gentle per-effect debounce so rapid toddler button mashing never clips.
 */
class SoundEngine(private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = ConcurrentHashMap<SoundEffect, Int>()
    private val lastPlayedMs = ConcurrentHashMap<SoundEffect, Long>()
    private val loader = Executors.newSingleThreadExecutor()

    init {
        loader.execute { loadAllSounds() }
    }

    private fun loadAllSounds() {
        val dir = File(context.cacheDir, "toybox_sfx").apply { mkdirs() }
        val clips = linkedMapOf(
            SoundEffect.PLOP to PcmSynth.softPlop(),
            SoundEffect.CHIME to PcmSynth.happyChime(),
            SoundEffect.POP to PcmSynth.balloonPop(),
            SoundEffect.PEEKABOO to PcmSynth.peekabooTada(),
            SoundEffect.FIREWORK_BOOM to PcmSynth.fireworkBoom(),
            SoundEffect.DRUM_KICK to PcmSynth.drumKick(),
            SoundEffect.DRUM_SNARE to PcmSynth.drumSnare(),
            SoundEffect.DRUM_TOM to PcmSynth.drumTom(),
            SoundEffect.DRUM_CYMBAL to PcmSynth.drumCymbal(),
            SoundEffect.WATER_SPLASH to PcmSynth.waterSplash(),
            SoundEffect.CRUNCH to PcmSynth.yummyCrunch(),
            SoundEffect.SPLAT to PcmSynth.paintSplat(),
            SoundEffect.DINO_STOMP to PcmSynth.dinoStomp(),
            SoundEffect.SPARKLE to PcmSynth.sparkleGliss(),
            SoundEffect.ANIMAL_BOING to PcmSynth.animalBoing(),
            SoundEffect.HONK to PcmSynth.carHonk(),
            SoundEffect.SIREN to PcmSynth.siren(),
            SoundEffect.TRAIN to PcmSynth.trainWhistle(),
            SoundEffect.ROCKET to PcmSynth.rocketWhoosh(),
            SoundEffect.NOTE_C5 to PcmSynth.xylophoneNote(523.25),
            SoundEffect.NOTE_D5 to PcmSynth.xylophoneNote(587.33),
            SoundEffect.NOTE_E5 to PcmSynth.xylophoneNote(659.25),
            SoundEffect.NOTE_F5 to PcmSynth.xylophoneNote(698.46),
            SoundEffect.NOTE_G5 to PcmSynth.xylophoneNote(783.99),
            SoundEffect.NOTE_A5 to PcmSynth.xylophoneNote(880.00),
            SoundEffect.NOTE_B5 to PcmSynth.xylophoneNote(987.77),
            SoundEffect.NOTE_C6 to PcmSynth.xylophoneNote(1046.50)
        )

        clips.forEach { (effect, wavBytes) ->
            val file = File(dir, "${effect.name.lowercase()}.wav")
            if (!file.exists() || file.length() != wavBytes.size.toLong()) {
                file.writeBytes(wavBytes)
            }
            val id = soundPool.load(file.absolutePath, 1)
            soundIds[effect] = id
        }
    }

    fun play(effect: SoundEffect, rate: Float = 1.0f, volume: Float = 0.9f) {
        val id = soundIds[effect] ?: return
        val now = System.currentTimeMillis()
        val prev = lastPlayedMs[effect] ?: 0L
        if (now - prev < 75L) return
        lastPlayedMs[effect] = now
        soundPool.play(id, volume, volume, 1, 0, rate.coerceIn(0.6f, 1.6f))
    }

    fun release() {
        loader.shutdownNow()
        soundPool.release()
    }
}
