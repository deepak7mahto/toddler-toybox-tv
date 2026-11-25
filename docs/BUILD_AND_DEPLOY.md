# Build & Deploy Guide

## Prerequisites
- JDK 17 (`JAVA_HOME` pointing to a JDK 17 installation)
- Android SDK (`ANDROID_HOME`)
- ADB (`platform-tools/adb`)

## Run Unit Tests
```bash
./gradlew test --no-daemon
```

## Build Optimized R8 Release APK (~797 KB)
```bash
./gradlew assembleRelease --no-daemon
# Output: app/build/outputs/apk/release/app-release.apk
```

## Sideload & AOT-Compile to Android TV (via ADB)
Running `cmd package compile -m speed -f com.toybox.tv` after install AOT-compiles the APK into native machine code for fast (~700ms) cold starts on Android TV hardware.

```bash
adb connect <TV_IP>:5555 && \
adb -s <TV_IP>:5555 install -r app/build/outputs/apk/release/app-release.apk && \
adb -s <TV_IP>:5555 shell cmd package compile -m speed -f com.toybox.tv && \
adb -s <TV_IP>:5555 shell am start -W -n com.toybox.tv/.MainActivity
```

## Uninstall
```bash
adb -s <TV_IP>:5555 uninstall com.toybox.tv
```
