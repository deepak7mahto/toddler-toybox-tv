# Architecture — Toddler Toybox TV (v1.4.0)

## Tech Stack
- **Language:** Kotlin 2.1.0 (JVM 17)
- **UI & Graphics:** Jetpack Compose + `Canvas` (60fps `withFrameNanos` animation loop)
- **Audio:** Procedural 16-bit mono PCM WAV synthesizer loaded into `android.media.SoundPool` (10 simultaneous polyphonic streams, zero external audio assets) + deferred offline Android `TextToSpeech`
- **Compatibility:** `minSdk 26` (Android 8.0+ TVs), `compileSdk 35`, `targetSdk 35`
- **Privacy:** Zero permissions requested (no `INTERNET` permission). 100% on-device.

## Package Structure
```text
app/src/main/java/com/toybox/tv/
├── MainActivity.kt                  # TV Activity, hardware key capture, instant BACK switch, 10s Demo loop
├── audio/
│   ├── PcmSynth.kt                  # Pure-Kotlin 16-bit PCM WAV synthesis (22 sound effects + C5..C6 octave)
│   ├── SoundEngine.kt               # SoundPool loader + polyphonic playback + 75ms mash debounce
│   └── VoiceSpeaker.kt              # Deferred offline Android TextToSpeech wrapper
├── input/
│   ├── ToyboxKey.kt                 # Normalized TV remote actions (UP, DOWN, LEFT, RIGHT, OK)
│   ├── HoldBackController.kt        # Double-BACK Home exit controller
│   └── DemoDirector.kt              # Pure non-repeating random game picker & per-mode auto-play cadence
└── ui/
    ├── theme/ToyboxTheme.kt         # Warm storybook palette & overscan-safe constants
    ├── components/
    │   ├── HoldBackIndicator.kt     # Optional circular indicator component
    │   └── ParticleSystem.kt        # Zero-GC reused-Path 60fps particle burst engine
    └── screens/
        ├── ToyboxHomeScreen.kt      # GPU-cached 6x3 grid (18 games) + Surprise Show banner
        ├── BalloonPopScreen.kt
        ├── AnimalFriendsScreen.kt
        ├── RainbowMusicScreen.kt
        ├── HonkAndGoScreen.kt
        ├── NurseryPianoScreen.kt
        ├── FireworksSkyScreen.kt
        ├── PeekABooScreen.kt
        ├── ColorsShapesScreen.kt
        ├── AbcScreen.kt
        ├── Numbers100Screen.kt
        ├── DrumBandScreen.kt
        ├── OceanFriendsScreen.kt
        ├── WeatherMagicScreen.kt
        ├── DinosaurStompScreen.kt
        ├── YummyFoodScreen.kt
        ├── PaintSplashScreen.kt
        ├── SpaceExplorerScreen.kt
        └── ButterflyGardenScreen.kt
```

## Performance Optimizations (v1.2.1+)
- **R8 Release + Resource Shrinking:** Shrinks APK from `8.4 MB` (debug) down to **`797 KB`**.
- **ART `speed` AOT Compilation:** Compiled on-device via `cmd package compile -m speed -f com.toybox.tv` (~`700 ms` cold start on Android TV hardware).
- **GPU-Only Home Grid:** Removed per-tile `Modifier.shadow()` passes; uses `graphicsLayer` scale transforms and `drawWithCache` for static paths.
- **Zero-GC Particles:** Reuses a single `Path` in `ParticleSystem.drawStar` to avoid per-frame allocations.
- **Deferred TTS:** Delays `TextToSpeech` binder initialization by 300ms so the first Home frame renders immediately.
