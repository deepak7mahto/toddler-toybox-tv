# Architecture — Toddler Toybox TV (v1.5.0)

## Tech Stack
- **Language:** Kotlin 2.1.0 (JVM 17)
- **UI & Graphics:** Jetpack Compose + `Canvas` (60fps `withFrameNanos` animation loop)
- **Hybrid Offline Audio:**
  - Procedural 16-bit mono PCM WAV synthesizer (`PcmSynth.kt` — 30 musical notes, horns, drums, and firecracker effects)
  - 21 real animal, bird, nature, and cartoon recordings in `res/raw/sfx_*.mp3` (`ffmpeg`-trimmed to 2.5s, `loudnorm`-normalized, 22 kHz mono 48 kbps = **`284 KB`** total)
  - Loaded into a 10-stream polyphonic `android.media.SoundPool` + deferred offline Android `TextToSpeech`
- **Compatibility:** `minSdk 26` (Android 8.0+ TVs), `compileSdk 35`, `targetSdk 35`
- **Privacy:** Zero permissions requested (no `INTERNET` permission). 100% on-device.

## Package Structure
```text
app/src/main/java/com/toybox/tv/
├── MainActivity.kt                  # TV Activity, hardware key capture, instant BACK switch, 10s Demo loop
├── audio/
│   ├── PcmSynth.kt                  # Pure-Kotlin 16-bit PCM WAV synthesis (30 sound effects + C5..C6 octave)
│   ├── SoundEngine.kt               # Polyphonic SoundPool loader (30 synth WAVs + 21 real res/raw MP3s)
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
    └── screens/                     # 24 play mode screens + GPU-cached 6x4 ToyboxHomeScreen
```

## Performance Optimizations
- **R8 Release + Resource Shrinking:** Entire 24-game app + 21 real MP3s compiles to **`1.1 MB`**.
- **ART `speed` AOT Compilation:** Compiled on-device via `cmd package compile -m speed -f com.toybox.tv` (~`700–850 ms` cold start on Android TV hardware).
- **GPU-Only Home Grid:** Removed per-tile `Modifier.shadow()` passes; uses `graphicsLayer` scale transforms and `drawWithCache` for static paths.
- **Zero-GC Particles:** Reuses a single `Path` in `ParticleSystem.drawStar` to avoid per-frame allocations.
- **Deferred TTS:** Delays `TextToSpeech` binder initialization by 300ms so the first Home frame renders immediately.
