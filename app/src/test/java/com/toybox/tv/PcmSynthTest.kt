package com.toybox.tv

import com.toybox.tv.audio.PcmSynth
import com.toybox.tv.ui.screens.AlphabetDeck
import com.toybox.tv.ui.screens.NumberWords
import com.toybox.tv.ui.screens.NurserySongs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmSynthTest {

    @Test
    fun generatedWavClipsHaveValidRiffHeadersAndAudioPayload() {
        val clips = listOf(
            PcmSynth.balloonPop(),
            PcmSynth.softPlop(),
            PcmSynth.happyChime(),
            PcmSynth.peekabooTada(),
            PcmSynth.fireworkBoom(),
            PcmSynth.drumKick(),
            PcmSynth.drumSnare(),
            PcmSynth.drumTom(),
            PcmSynth.drumCymbal(),
            PcmSynth.waterSplash(),
            PcmSynth.yummyCrunch(),
            PcmSynth.paintSplat(),
            PcmSynth.dinoStomp(),
            PcmSynth.sparkleGliss(),
            PcmSynth.chakkarSpin(),
            PcmSynth.anarFountain(),
            PcmSynth.sparklerCrackle(),
            PcmSynth.animalBoing(),
            PcmSynth.carHonk(),
            PcmSynth.siren(),
            PcmSynth.trainWhistle(),
            PcmSynth.rocketWhoosh(),
            PcmSynth.xylophoneNote(523.25),
            PcmSynth.xylophoneNote(1046.50)
        )

        for (wav in clips) {
            assertTrue("WAV must exceed 44-byte header", wav.size > 44)
            assertEquals("RIFF", String(wav, 0, 4))
            assertEquals("WAVE", String(wav, 8, 4))
            assertEquals("fmt ", String(wav, 12, 4))
            assertEquals("data", String(wav, 36, 4))
        }
    }

    @Test
    fun nurserySongsReferenceValidScaleIndices() {
        val maxIndex = NurserySongs.scale.lastIndex
        assertTrue(NurserySongs.all.size >= 4)
        for (song in NurserySongs.all) {
            assertTrue("${song.title} must have notes", song.notes.isNotEmpty())
            for (noteIdx in song.notes) {
                assertTrue("Note index $noteIdx out of range 0..$maxIndex", noteIdx in 0..maxIndex)
            }
        }
    }

    @Test
    fun alphabetDeckCoversAll26Letters() {
        assertEquals(26, AlphabetDeck.all.size)
        assertEquals("A", AlphabetDeck.all.first().letter)
        assertEquals("Z", AlphabetDeck.all.last().letter)
    }

    @Test
    fun numberWordsSpellOneThroughOneHundredAccurately() {
        assertEquals("One", NumberWords.toWords(1))
        assertEquals("Ten", NumberWords.toWords(10))
        assertEquals("Fifteen", NumberWords.toWords(15))
        assertEquals("Twenty", NumberWords.toWords(20))
        assertEquals("Forty Two", NumberWords.toWords(42))
        assertEquals("Ninety Nine", NumberWords.toWords(99))
        assertEquals("One Hundred", NumberWords.toWords(100))
    }
}
