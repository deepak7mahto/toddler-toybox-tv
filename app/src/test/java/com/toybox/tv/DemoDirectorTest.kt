package com.toybox.tv

import com.toybox.tv.input.DemoDirector
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.screens.ToyboxMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class DemoDirectorTest {

    @Test
    fun nextRandomModeNeverRepeatsCurrentMode() {
        val rng = Random(42)
        var current = ToyboxMode.BALLOON_POP
        repeat(50) {
            val next = DemoDirector.nextRandomMode(current, rng)
            assertNotEquals(current, next)
            current = next
        }
    }

    @Test
    fun nurseryPianoDemoAlwaysPlaysMelodyKey() {
        repeat(10) { step ->
            assertEquals(ToyboxKey.OK, DemoDirector.nextActionFor(ToyboxMode.NURSERY_PIANO, step))
        }
    }
}
