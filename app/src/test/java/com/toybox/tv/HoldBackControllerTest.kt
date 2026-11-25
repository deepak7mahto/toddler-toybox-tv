package com.toybox.tv

import com.toybox.tv.input.HoldBackController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoldBackControllerTest {

    @Test
    fun shortBackTapInsideModeDoesNotTriggerExit() {
        val controller = HoldBackController(holdDurationMs = 2000L)
        controller.onBackDown(nowMs = 1000L)
        val mid = controller.updateHold(nowMs = 1500L)
        assertTrue(mid.isHolding)
        assertEquals(0.25f, mid.progress, 0.001f)
        assertFalse(mid.triggered)

        controller.onBackUp()
        val afterUp = controller.updateHold(nowMs = 3500L)
        assertFalse(afterUp.isHolding)
        assertEquals(0f, afterUp.progress, 0.001f)
        assertFalse(afterUp.triggered)
    }

    @Test
    fun continuousTwoSecondHoldTriggersOnce() {
        val controller = HoldBackController(holdDurationMs = 2000L)
        controller.onBackDown(nowMs = 1000L)
        // OS key repeats should not reset the start timestamp
        controller.onBackDown(nowMs = 1600L)

        val done = controller.updateHold(nowMs = 3000L)
        assertTrue(done.triggered)
        assertEquals(1f, done.progress, 0.001f)

        // Subsequent frames while still held do not re-trigger
        val next = controller.updateHold(nowMs = 3200L)
        assertFalse(next.triggered)
    }

    @Test
    fun homeDoubleBackRequiresTwoTapsWithinWindow() {
        val controller = HoldBackController(doubleBackWindowMs = 1500L)
        assertFalse(controller.onHomeBackPress(nowMs = 1000L))
        assertFalse(controller.onHomeBackPress(nowMs = 3000L)) // Outside 1500ms window
        assertTrue(controller.onHomeBackPress(nowMs = 4200L))  // Within 1500ms of 3000ms
    }
}
