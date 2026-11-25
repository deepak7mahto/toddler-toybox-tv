package com.toybox.tv.input

import com.toybox.tv.ui.screens.ToyboxMode
import kotlin.random.Random

/**
 * Pure helper powering the 10-second Surprise Show (Demo Mode).
 * Picks non-repeating random games and playful remote actions per mode.
 */
object DemoDirector {
    const val DEMO_DURATION_SEC = 10

    fun nextRandomMode(current: ToyboxMode?, rng: Random = Random.Default): ToyboxMode {
        val candidates = ToyboxMode.entries.filter { it != current }
        return candidates[rng.nextInt(candidates.size)]
    }

    fun nextActionFor(mode: ToyboxMode, step: Int, rng: Random = Random.Default): ToyboxKey {
        return when (mode) {
            // Keep Nursery Piano playing the melody smoothly on every beat
            ToyboxMode.NURSERY_PIANO -> ToyboxKey.OK
            // Alternate peek & hide in Peek-a-Boo, occasionally switching hiding spot
            ToyboxMode.PEEK_A_BOO -> if (step % 4 == 3) ToyboxKey.RIGHT else ToyboxKey.OK
            // Step forward through A-Z and 1-100 cleanly
            ToyboxMode.ABC_A_TO_Z, ToyboxMode.NUMBERS_100 -> ToyboxKey.RIGHT
            // Mix bouncing & switching animals/vehicles/ocean/weather/dinos/food/space
            ToyboxMode.ANIMAL_FRIENDS,
            ToyboxMode.HONK_AND_GO,
            ToyboxMode.OCEAN_FRIENDS,
            ToyboxMode.WEATHER_MAGIC,
            ToyboxMode.DINOSAUR_STOMP,
            ToyboxMode.YUMMY_FOOD,
            ToyboxMode.SPACE_EXPLORER ->
                if (step % 2 == 0) ToyboxKey.OK else ToyboxKey.RIGHT
            // Full colorful variety for balloons, xylophone, fireworks, shapes, drums, paint, garden
            else -> {
                val keys = ToyboxKey.entries
                keys[rng.nextInt(keys.size)]
            }
        }
    }

    fun actionIntervalMs(mode: ToyboxMode): Long = when (mode) {
        ToyboxMode.BALLOON_POP,
        ToyboxMode.RAINBOW_MUSIC,
        ToyboxMode.NURSERY_PIANO,
        ToyboxMode.FIREWORKS_SKY,
        ToyboxMode.DRUM_BAND,
        ToyboxMode.PAINT_SPLASH,
        ToyboxMode.BUTTERFLY_GARDEN -> 650L
        else -> 1300L
    }
}
