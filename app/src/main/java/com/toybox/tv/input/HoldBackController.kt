package com.toybox.tv.input

/**
 * Pure state machine implementing mash-safe BACK handling for toddlers:
 * - Inside a play mode: BACK must be held continuously for [holdDurationMs] (default 2000ms)
 *   to return to the Toybox Picker. Short taps are safely ignored.
 * - On the Home screen: BACK must be pressed twice within [doubleBackWindowMs] (default 1500ms)
 *   to exit the app.
 */
class HoldBackController(
    private val holdDurationMs: Long = 2000L,
    private val doubleBackWindowMs: Long = 1500L
) {
    private var holdStartMs: Long? = null
    private var completedCurrentHold: Boolean = false
    private var lastHomeBackMs: Long? = null

    val isHolding: Boolean
        get() = holdStartMs != null

    /**
     * Call when BACK key goes down inside a play mode.
     * Repeated key-down events (from OS key repeat) are ignored while already holding.
     */
    fun onBackDown(nowMs: Long) {
        if (holdStartMs == null) {
            holdStartMs = nowMs
            completedCurrentHold = false
        }
    }

    /**
     * Call periodically (e.g. every animation frame) while inside a play mode.
     * Returns a pair of (progress in 0f..1f, completedNow).
     */
    fun updateHold(nowMs: Long): HoldState {
        val start = holdStartMs ?: return HoldState(isHolding = false, progress = 0f, triggered = false)
        if (completedCurrentHold) {
            return HoldState(isHolding = true, progress = 1f, triggered = false)
        }
        val elapsed = (nowMs - start).coerceAtLeast(0L)
        val progress = (elapsed.toFloat() / holdDurationMs.toFloat()).coerceIn(0f, 1f)
        if (elapsed >= holdDurationMs) {
            completedCurrentHold = true
            return HoldState(isHolding = true, progress = 1f, triggered = true)
        }
        return HoldState(isHolding = true, progress = progress, triggered = false)
    }

    /**
     * Call when BACK key is released. Resets the hold timer.
     */
    fun onBackUp() {
        holdStartMs = null
        completedCurrentHold = false
    }

    /**
     * Resets all hold/double-back state (e.g. when switching screens).
     */
    fun reset() {
        holdStartMs = null
        completedCurrentHold = false
        lastHomeBackMs = null
    }

    /**
     * Call on BACK key down while on the Home screen.
     * Returns true if this is the second tap within [doubleBackWindowMs].
     */
    fun onHomeBackPress(nowMs: Long): Boolean {
        val prev = lastHomeBackMs
        return if (prev != null && (nowMs - prev) in 0..doubleBackWindowMs) {
            lastHomeBackMs = null
            true
        } else {
            lastHomeBackMs = nowMs
            false
        }
    }
}

data class HoldState(
    val isHolding: Boolean,
    val progress: Float,
    val triggered: Boolean
)
