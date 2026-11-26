package com.toybox.tv.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.toybox.tv.R
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
    CHAKKAR_SPIN,
    ANAR_FOUNTAIN,
    SPARKLER_CRACKLE,
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
    NOTE_C6,
    // Real recorded & ffmpeg-optimized offline MP3 assets (res/raw/)
    REAL_LION,
    REAL_ELEPHANT,
    REAL_DUCK,
    REAL_COW,
    REAL_DOG,
    REAL_CAT,
    REAL_OWL,
    REAL_MOUSE,
    REAL_BEE,
    REAL_SONGBIRD,
    REAL_BIRD_CHIRP,
    REAL_CROW,
    REAL_RAIN,
    REAL_THUNDER,
    REAL_WAVES,
    REAL_BELL,
    REAL_BOING,
    REAL_COWBELL,
    REAL_CORK_POP,
    REAL_WOOD_PLANK,
    REAL_SIREN
}

/**
 * Polyphonic, 100% offline SoundPool engine combining procedural PCM WAV effects
 * with 21 real ffmpeg-optimized animal, bird, nature, and cartoon MP3 recordings.
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
        val synthClips = linkedMapOf(
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
            SoundEffect.CHAKKAR_SPIN to PcmSynth.chakkarSpin(),
            SoundEffect.ANAR_FOUNTAIN to PcmSynth.anarFountain(),
            SoundEffect.SPARKLER_CRACKLE to PcmSynth.sparklerCrackle(),
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

        synthClips.forEach { (effect, wavBytes) ->
            val file = File(dir, "${effect.name.lowercase()}.wav")
            if (!file.exists() || file.length() != wavBytes.size.toLong()) {
                file.writeBytes(wavBytes)
            }
            soundIds[effect] = soundPool.load(file.absolutePath, 1)
        }

        val rawClips = mapOf(
            SoundEffect.REAL_LION to R.raw.sfx_lion,
            SoundEffect.REAL_ELEPHANT to R.raw.sfx_elephant,
            SoundEffect.REAL_DUCK to R.raw.sfx_duck,
            SoundEffect.REAL_COW to R.raw.sfx_cow,
            SoundEffect.REAL_DOG to R.raw.sfx_dog,
            SoundEffect.REAL_CAT to R.raw.sfx_cat,
            SoundEffect.REAL_OWL to R.raw.sfx_owl,
            SoundEffect.REAL_MOUSE to R.raw.sfx_mouse,
            SoundEffect.REAL_BEE to R.raw.sfx_bee,
            SoundEffect.REAL_SONGBIRD to R.raw.sfx_songbird,
            SoundEffect.REAL_BIRD_CHIRP to R.raw.sfx_bird_chirp,
            SoundEffect.REAL_CROW to R.raw.sfx_crow,
            SoundEffect.REAL_RAIN to R.raw.sfx_rain,
            SoundEffect.REAL_THUNDER to R.raw.sfx_thunder,
            SoundEffect.REAL_WAVES to R.raw.sfx_waves,
            SoundEffect.REAL_BELL to R.raw.sfx_bell,
            SoundEffect.REAL_BOING to R.raw.sfx_cartoon_boing,
            SoundEffect.REAL_COWBELL to R.raw.sfx_cartoon_cowbell,
            SoundEffect.REAL_CORK_POP to R.raw.sfx_pop_cork,
            SoundEffect.REAL_WOOD_PLANK to R.raw.sfx_wood_plank,
            SoundEffect.REAL_SIREN to R.raw.sfx_siren_real
        )

        rawClips.forEach { (effect, resId) ->
            soundIds[effect] = soundPool.load(context, resId, 1)
        }
    }

    fun play(effect: SoundEffect, rate: Float = 1.0f, volume: Float = 0.92f) {
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
