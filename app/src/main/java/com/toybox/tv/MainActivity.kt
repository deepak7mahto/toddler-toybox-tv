package com.toybox.tv

import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.DemoDirector
import com.toybox.tv.input.HoldBackController
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.screens.AbcScreen
import com.toybox.tv.ui.screens.AnarFountainScreen
import com.toybox.tv.ui.screens.AnimalFriendsScreen
import com.toybox.tv.ui.screens.BalloonPopScreen
import com.toybox.tv.ui.screens.BirdSongsScreen
import com.toybox.tv.ui.screens.ButterflyGardenScreen
import com.toybox.tv.ui.screens.CartoonSoundsScreen
import com.toybox.tv.ui.screens.ChakkarSpinnerScreen
import com.toybox.tv.ui.screens.ColorsShapesScreen
import com.toybox.tv.ui.screens.DinosaurStompScreen
import com.toybox.tv.ui.screens.DrumBandScreen
import com.toybox.tv.ui.screens.FireworksSkyScreen
import com.toybox.tv.ui.screens.HonkAndGoScreen
import com.toybox.tv.ui.screens.NatureSoundsScreen
import com.toybox.tv.ui.screens.Numbers100Screen
import com.toybox.tv.ui.screens.NurseryPianoScreen
import com.toybox.tv.ui.screens.OceanFriendsScreen
import com.toybox.tv.ui.screens.PaintSplashScreen
import com.toybox.tv.ui.screens.PeekABooScreen
import com.toybox.tv.ui.screens.RainbowMusicScreen
import com.toybox.tv.ui.screens.SpaceExplorerScreen
import com.toybox.tv.ui.screens.SparklerMagicScreen
import com.toybox.tv.ui.screens.ToyboxHomeScreen
import com.toybox.tv.ui.screens.ToyboxMode
import com.toybox.tv.ui.screens.WeatherMagicScreen
import com.toybox.tv.ui.screens.YummyFoodScreen
import com.toybox.tv.ui.theme.ToddlerToyboxTheme
import com.toybox.tv.ui.theme.ToyboxColors
import com.toybox.tv.ui.theme.TvOverscan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var soundEngine: SoundEngine
    private lateinit var voiceSpeaker: VoiceSpeaker
    private val holdBackController = HoldBackController()

    private var activeMode by mutableStateOf<ToyboxMode?>(null)
    private var homeSelectedIndex by mutableIntStateOf(0) // -1 = Demo button, 0..11 = 4x3 grid
    private var showExitHint by mutableStateOf(false)
    private var keyCounter by mutableIntStateOf(0)
    private var lastModeKey by mutableStateOf<Pair<Int, ToyboxKey>?>(null)

    private var isDemoMode by mutableStateOf(false)
    private var demoSecondsLeft by mutableIntStateOf(DemoDirector.DEMO_DURATION_SEC)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        soundEngine = SoundEngine(this)
        voiceSpeaker = VoiceSpeaker(this)

        setContent {
            ToddlerToyboxTheme {
                // Auto-hide the Home double-BACK hint after 1.6s
                LaunchedEffect(showExitHint) {
                    if (showExitHint) {
                        delay(1600L)
                        showExitHint = false
                    }
                }

                // 10-second Surprise Show (Demo Mode) auto-play & random game switcher
                LaunchedEffect(isDemoMode, activeMode) {
                    val mode = activeMode
                    if (!isDemoMode || mode == null) return@LaunchedEffect

                    val autoPlayJob = launch {
                        val interval = DemoDirector.actionIntervalMs(mode)
                        var step = 0
                        delay(350L)
                        while (isDemoMode && activeMode == mode) {
                            keyCounter++
                            lastModeKey = keyCounter to DemoDirector.nextActionFor(mode, step++)
                            delay(interval)
                        }
                    }

                    for (sec in DemoDirector.DEMO_DURATION_SEC downTo 1) {
                        demoSecondsLeft = sec
                        delay(1000L)
                        if (!isDemoMode || activeMode != mode) {
                            autoPlayJob.cancel()
                            return@LaunchedEffect
                        }
                    }

                    autoPlayJob.cancel()
                    // 10 seconds done -> switch to a random new game!
                    val nextMode = DemoDirector.nextRandomMode(mode)
                    soundEngine.play(SoundEffect.CHIME)
                    lastModeKey = null
                    activeMode = nextMode
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (activeMode) {
                        null -> ToyboxHomeScreen(
                            selectedIndex = homeSelectedIndex,
                            showExitHint = showExitHint,
                            onSelectMode = { mode -> launchMode(mode, demo = false) },
                            onStartDemo = { startDemoMode() }
                        )
                        ToyboxMode.BALLOON_POP -> BalloonPopScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.ANIMAL_FRIENDS -> AnimalFriendsScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.RAINBOW_MUSIC -> RainbowMusicScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.HONK_AND_GO -> HonkAndGoScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.NURSERY_PIANO -> NurseryPianoScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.FIREWORKS_SKY -> FireworksSkyScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.PEEK_A_BOO -> PeekABooScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.COLORS_SHAPES -> ColorsShapesScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.ABC_A_TO_Z -> AbcScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.NUMBERS_100 -> Numbers100Screen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.DRUM_BAND -> DrumBandScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.OCEAN_FRIENDS -> OceanFriendsScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.WEATHER_MAGIC -> WeatherMagicScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.DINOSAUR_STOMP -> DinosaurStompScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.YUMMY_FOOD -> YummyFoodScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.PAINT_SPLASH -> PaintSplashScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.SPACE_EXPLORER -> SpaceExplorerScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.BUTTERFLY_GARDEN -> ButterflyGardenScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.CHAKKAR_SPINNER -> ChakkarSpinnerScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.ANAR_FOUNTAIN -> AnarFountainScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.SPARKLER_MAGIC -> SparklerMagicScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.BIRD_SONGS -> BirdSongsScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                        ToyboxMode.CARTOON_SOUNDS -> CartoonSoundsScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine
                        )
                        ToyboxMode.NATURE_SOUNDS -> NatureSoundsScreen(
                            lastKeyEvent = lastModeKey,
                            soundEngine = soundEngine,
                            voiceSpeaker = voiceSpeaker
                        )
                    }

                    // Subtle top-right countdown badge during Demo Mode
                    if (isDemoMode && activeMode != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(
                                    end = TvOverscan.Horizontal,
                                    top = TvOverscan.Vertical
                                )
                                .background(Color(0xAA1A237E), RoundedCornerShape(24.dp))
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "▶ DEMO  •  ${demoSecondsLeft}s",
                                color = ToyboxColors.FocusGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }

    private fun launchMode(mode: ToyboxMode, demo: Boolean) {
        soundEngine.play(SoundEffect.CHIME)
        isDemoMode = demo
        demoSecondsLeft = DemoDirector.DEMO_DURATION_SEC
        lastModeKey = null
        activeMode = mode
    }

    private fun startDemoMode() {
        val first = DemoDirector.nextRandomMode(null)
        launchMode(first, demo = true)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            return super.dispatchKeyEvent(event)
        }

        // Play/Pause shortcut on remote starts Surprise Show (Demo Mode) from Home
        if ((keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) &&
            event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
        ) {
            if (activeMode == null) {
                startDemoMode()
                return true
            }
        }

        // Single BACK tap inside any game immediately stops Demo/game and returns to Home
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                if (activeMode != null) {
                    soundEngine.play(SoundEffect.PLOP)
                    isDemoMode = false
                    activeMode = null
                    lastModeKey = null
                    holdBackController.reset()
                } else {
                    if (holdBackController.onHomeBackPress(SystemClock.elapsedRealtime())) {
                        finish()
                    } else {
                        soundEngine.play(SoundEffect.PLOP)
                        showExitHint = true
                    }
                }
            }
            return true
        }

        val mappedKey = mapKeyCode(keyCode) ?: return super.dispatchKeyEvent(event)

        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (activeMode == null) {
                handleHomeNavigation(mappedKey)
            } else {
                keyCounter++
                lastModeKey = keyCounter to mappedKey
            }
        }
        return true
    }

    private fun handleHomeNavigation(key: ToyboxKey) {
        val cols = 6
        val total = ToyboxMode.entries.size

        if (homeSelectedIndex == -1) {
            when (key) {
                ToyboxKey.DOWN -> {
                    homeSelectedIndex = 0
                    soundEngine.play(SoundEffect.PLOP)
                }
                ToyboxKey.OK -> startDemoMode()
                else -> Unit
            }
            return
        }

        val next = when (key) {
            ToyboxKey.LEFT -> if (homeSelectedIndex % cols > 0) homeSelectedIndex - 1 else homeSelectedIndex
            ToyboxKey.RIGHT -> if (homeSelectedIndex % cols < cols - 1 && homeSelectedIndex + 1 < total) homeSelectedIndex + 1 else homeSelectedIndex
            ToyboxKey.UP -> if (homeSelectedIndex >= cols) homeSelectedIndex - cols else -1
            ToyboxKey.DOWN -> if (homeSelectedIndex + cols < total) homeSelectedIndex + cols else homeSelectedIndex
            ToyboxKey.OK -> {
                launchMode(ToyboxMode.entries[homeSelectedIndex], demo = false)
                return
            }
        }
        if (next != homeSelectedIndex) {
            homeSelectedIndex = next
            soundEngine.play(SoundEffect.PLOP)
        }
    }

    private fun mapKeyCode(keyCode: Int): ToyboxKey? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> ToyboxKey.UP
        KeyEvent.KEYCODE_DPAD_DOWN -> ToyboxKey.DOWN
        KeyEvent.KEYCODE_DPAD_LEFT -> ToyboxKey.LEFT
        KeyEvent.KEYCODE_DPAD_RIGHT -> ToyboxKey.RIGHT
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_SPACE,
        KeyEvent.KEYCODE_BUTTON_A -> ToyboxKey.OK
        else -> null
    }

    override fun onDestroy() {
        soundEngine.release()
        voiceSpeaker.release()
        super.onDestroy()
    }
}
