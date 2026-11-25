package com.toybox.tv.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure-Kotlin 16-bit mono PCM WAV synthesizer.
 * Generates cheerful, zero-asset sound effects for Toddler Toybox TV.
 */
object PcmSynth {
    const val SAMPLE_RATE = 22050

    fun xylophoneNote(freqHz: Double, durationSec: Double = 0.55): ByteArray {
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (t / 0.006).coerceAtMost(1.0)
            val env = attack * exp(-t * 5.2)
            val fundamental = sin(2.0 * PI * freqHz * t)
            val harmonic2 = 0.28 * sin(2.0 * PI * (freqHz * 2.0) * t) * exp(-t * 8.0)
            val harmonic3 = 0.12 * sin(2.0 * PI * (freqHz * 3.0) * t) * exp(-t * 14.0)
            val value = (fundamental + harmonic2 + harmonic3) * env * 0.72
            samples[i] = toPcm16(value)
        }
        return encodeWav(samples)
    }

    fun balloonPop(): ByteArray {
        val durationSec = 0.16
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(42)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 520.0 * exp(-t * 28.0) + 95.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val snap = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 55.0) * 0.55
            val body = sin(phase) * exp(-t * 22.0) * 0.85
            samples[i] = toPcm16((body + snap) * 0.85)
        }
        return encodeWav(samples)
    }

    fun softPlop(): ByteArray {
        val durationSec = 0.11
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 320.0 + 260.0 * (t / durationSec)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = sin(PI * t / durationSec) * exp(-t * 10.0)
            samples[i] = toPcm16(sin(phase) * env * 0.6)
        }
        return encodeWav(samples)
    }

    fun happyChime(): ByteArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val durationSec = 0.65
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val stepSec = 0.085
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0
            for (n in notes.indices) {
                val nt = t - n * stepSec
                if (nt >= 0.0) {
                    val env = (nt / 0.005).coerceAtMost(1.0) * exp(-nt * 6.0)
                    sum += sin(2.0 * PI * notes[n] * nt) * env * 0.35
                }
            }
            samples[i] = toPcm16(sum)
        }
        return encodeWav(samples)
    }

    fun peekabooTada(): ByteArray {
        val durationSec = 0.42
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val n1 = if (t < 0.15) sin(2.0 * PI * 587.33 * t) * exp(-t * 9.0) * 0.65 else 0.0
            val t2 = t - 0.12
            val n2 = if (t2 >= 0.0) {
                (sin(2.0 * PI * 880.0 * t2) + 0.3 * sin(2.0 * PI * 1318.5 * t2)) *
                    (t2 / 0.008).coerceAtMost(1.0) * exp(-t2 * 5.5) * 0.55
            } else {
                0.0
            }
            samples[i] = toPcm16(n1 + n2)
        }
        return encodeWav(samples)
    }

    fun fireworkBoom(): ByteArray {
        val durationSec = 0.55
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(123)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val thumpFreq = 160.0 * exp(-t * 14.0) + 48.0
            phase += 2.0 * PI * thumpFreq / SAMPLE_RATE
            val thump = sin(phase) * exp(-t * 9.0) * 0.68
            val crackle = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 6.5) * 0.35
            val sparkle = sin(2.0 * PI * (1200.0 + 400.0 * sin(t * 45.0)) * t) * exp(-t * 8.0) * 0.18
            samples[i] = toPcm16(thump + crackle + sparkle)
        }
        return encodeWav(samples)
    }

    fun drumKick(): ByteArray {
        val durationSec = 0.28
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 150.0 * exp(-t * 22.0) + 52.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            samples[i] = toPcm16(sin(phase) * exp(-t * 11.0) * 0.9)
        }
        return encodeWav(samples)
    }

    fun drumSnare(): ByteArray {
        val durationSec = 0.22
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(7)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val body = sin(2.0 * PI * 210.0 * t) * exp(-t * 24.0) * 0.55
            val wire = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 18.0) * 0.62
            samples[i] = toPcm16(body + wire)
        }
        return encodeWav(samples)
    }

    fun drumTom(): ByteArray {
        val durationSec = 0.30
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 240.0 * exp(-t * 10.0) + 125.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            samples[i] = toPcm16(sin(phase) * exp(-t * 9.5) * 0.8)
        }
        return encodeWav(samples)
    }

    fun drumCymbal(): ByteArray {
        val durationSec = 0.45
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(31)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val shimmer = (rng.nextDouble() * 2.0 - 1.0) * 0.45 +
                sin(2.0 * PI * 1760.0 * t) * 0.2 +
                sin(2.0 * PI * 2637.0 * t) * 0.15
            samples[i] = toPcm16(shimmer * exp(-t * 6.5) * 0.72)
        }
        return encodeWav(samples)
    }

    fun waterSplash(): ByteArray {
        val durationSec = 0.36
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(55)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val bubbleFreq = 380.0 + 520.0 * (t / durationSec) + 90.0 * sin(2.0 * PI * 22.0 * t)
            phase += 2.0 * PI * bubbleFreq / SAMPLE_RATE
            val bubble = sin(phase) * exp(-t * 8.0) * 0.55
            val spray = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 11.0) * 0.35
            samples[i] = toPcm16(bubble + spray)
        }
        return encodeWav(samples)
    }

    fun yummyCrunch(): ByteArray {
        val durationSec = 0.24
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(77)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val bite1 = if (t in 0.0..0.09) exp(-t * 28.0) else 0.0
            val bite2 = if (t in 0.11..0.22) exp(-(t - 0.11) * 26.0) else 0.0
            val noise = (rng.nextDouble() * 2.0 - 1.0) * (bite1 + bite2) * 0.72
            val snap = sin(2.0 * PI * 620.0 * t) * (bite1 + bite2) * 0.25
            samples[i] = toPcm16(noise + snap)
        }
        return encodeWav(samples)
    }

    fun paintSplat(): ByteArray {
        val durationSec = 0.22
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(88)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 190.0 + 360.0 * exp(-t * 18.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val squish = sin(phase) * exp(-t * 14.0) * 0.65
            val splash = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 22.0) * 0.45
            samples[i] = toPcm16(squish + splash)
        }
        return encodeWav(samples)
    }

    fun dinoStomp(): ByteArray {
        val durationSec = 0.42
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 115.0 * exp(-t * 9.0) + 46.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val thud = sin(phase) * exp(-t * 6.5) * 0.88
            samples[i] = toPcm16(thud)
        }
        return encodeWav(samples)
    }

    fun sparkleGliss(): ByteArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 987.77, 1174.66, 1318.51)
        val durationSec = 0.55
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0
            for (n in notes.indices) {
                val nt = t - n * 0.055
                if (nt >= 0.0) {
                    val env = (nt / 0.005).coerceAtMost(1.0) * exp(-nt * 8.0)
                    sum += sin(2.0 * PI * notes[n] * nt) * env * 0.28
                }
            }
            samples[i] = toPcm16(sum)
        }
        return encodeWav(samples)
    }

    fun animalBoing(): ByteArray {
        val durationSec = 0.38
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val wobble = sin(2.0 * PI * 14.0 * t) * 45.0 * exp(-t * 5.0)
            val freq = 260.0 + 340.0 * (1.0 - exp(-t * 9.0)) + wobble
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = (t / 0.01).coerceAtMost(1.0) * exp(-t * 4.5)
            samples[i] = toPcm16(sin(phase) * env * 0.72)
        }
        return encodeWav(samples)
    }

    fun carHonk(): ByteArray {
        val durationSec = 0.48
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val beep1 = if (t in 0.0..0.16) 1.0 else 0.0
            val beep2 = if (t in 0.22..0.44) 1.0 else 0.0
            val gate = (beep1 + beep2).coerceAtMost(1.0)
            val wave = (
                sin(2.0 * PI * 340.0 * t) * 0.55 +
                    sin(2.0 * PI * 425.0 * t) * 0.45 +
                    sin(2.0 * PI * 680.0 * t) * 0.18
                )
            samples[i] = toPcm16(wave * gate * 0.58)
        }
        return encodeWav(samples)
    }

    fun siren(): ByteArray {
        val durationSec = 0.75
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 640.0 + 220.0 * sin(2.0 * PI * 3.0 * t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = (t / 0.03).coerceAtMost(1.0) * ((durationSec - t) / 0.08).coerceIn(0.0, 1.0)
            samples[i] = toPcm16(sin(phase) * env * 0.55)
        }
        return encodeWav(samples)
    }

    fun trainWhistle(): ByteArray {
        val durationSec = 0.70
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val gate = when {
                t < 0.26 -> 1.0
                t < 0.34 -> 0.0
                t < 0.66 -> 1.0
                else -> 0.0
            }
            val chord = (
                sin(2.0 * PI * 587.33 * t) * 0.4 +
                    sin(2.0 * PI * 739.99 * t) * 0.35 +
                    sin(2.0 * PI * 880.00 * t) * 0.25
                )
            samples[i] = toPcm16(chord * gate * 0.62)
        }
        return encodeWav(samples)
    }

    fun rocketWhoosh(): ByteArray {
        val durationSec = 0.65
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val rng = Random(99)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val freq = 180.0 + 780.0 * progress * progress
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = sin(PI * progress)
            val noise = (rng.nextDouble() * 2.0 - 1.0) * 0.25
            val tone = sin(phase) * 0.6
            samples[i] = toPcm16((tone + noise) * env * 0.65)
        }
        return encodeWav(samples)
    }

    private fun toPcm16(normalized: Double): Short {
        val clamped = normalized.coerceIn(-0.98, 0.98)
        return (clamped * Short.MAX_VALUE).toInt().toShort()
    }

    fun encodeWav(samples: ShortArray): ByteArray {
        val dataSize = samples.size * 2
        val byteRate = SAMPLE_RATE * 2
        val out = ByteArrayOutputStream(44 + dataSize)

        fun writeAscii(s: String) = s.forEach { out.write(it.code) }
        fun writeIntLE(v: Int) {
            out.write(v and 0xFF)
            out.write((v ushr 8) and 0xFF)
            out.write((v ushr 16) and 0xFF)
            out.write((v ushr 24) and 0xFF)
        }
        fun writeShortLE(v: Int) {
            out.write(v and 0xFF)
            out.write((v ushr 8) and 0xFF)
        }

        writeAscii("RIFF")
        writeIntLE(36 + dataSize)
        writeAscii("WAVE")
        writeAscii("fmt ")
        writeIntLE(16)
        writeShortLE(1) // PCM
        writeShortLE(1) // Mono
        writeIntLE(SAMPLE_RATE)
        writeIntLE(byteRate)
        writeShortLE(2) // Block align
        writeShortLE(16) // Bits per sample
        writeAscii("data")
        writeIntLE(dataSize)

        for (s in samples) {
            writeShortLE(s.toInt())
        }
        return out.toByteArray()
    }
}
