# PebbleKitAndroid2 Old Message Re-broadcast Reproduction

## Description

This project reproduces a bug in the Pebble-to-Android communication protocol where old messages are re-broadcast when reopening the Pebble watch app.

The communication flow is simple:
- Press the "select" key on the Pebble watch to send a "ping" message to the Android mobile app
- The Android mobile app receives the ping and responds with a "pong" message
- The Pebble watch app renders the pong message in its UI

**The Bug:** If you keep the Android mobile app open while exiting and reopening the Pebble app, the Android app receives and processes old messages that should have already been consumed. This causes a flurry of messages to be re-sent to the Pebble app on re-launch.

## Reproduction Steps

1. Build and deploy both the Pebble app and Android mobile app
2. Open the Android mobile app and keep it running in the foreground
3. Open the Pebble watch app
4. Press the "select" key on the Pebble watch to send a ping
5. Observe the Android app logs and pong being rendered in the pebble app UI
6. Close/exit the Pebble watch app
7. Reopen the Pebble watch app which should just have a blank screen
8. **Bug observed:** The Android app rendered logs show it receives the old ping message again and sends a pong message to the pebble app. The pebble app UI shows a lonely pong message without a ping, which should never happen.

## Requirements

- **Java 21** - The mobile app requires Java 21 to build. Set your `JAVA_HOME` environment variable to point to a Java 21 installation before building.

## Building

### Mobile App

Navigate to the `mobile-app` directory and run:

```bash
cd mobile-app
./gradlew assembleDebug
```

This creates a debug APK for testing. For a release build, use:

```bash
./gradlew build
```

Alternatively, open the project in Android Studio and build from there.

The project uses gradle wrapper for reproducibility. Ensure you have Java 21 installed and configured.

### Pebble App

Navigate to the `pebble-app` directory and run:

```bash
cd pebble-app
pebble build
```
