# Bluetooth Auto Push (Android -> Ubuntu)

This Android app auto-attempts Bluetooth file transfer when a PC pairs or connects (might not work on all systems).

## Behavior

- Listens for:
  - Bluetooth bond success (pair completed)
  - Bluetooth ACL connect events
- Enqueues transfer work with retry logic.
- Builds payload from:
  - A generated text file (from app template text)
  - Files placed in app internal `queue` folder (`pdf`, `txt`, `jpg`, `png`)
- Tries sending via Android Bluetooth package (`com.android.bluetooth`) using OPP share intent.

## Important constraints

- Ubuntu user can be offline.
- Ubuntu user may still need to click receive/accept prompt.
- Android platform restrictions may still show device chooser/prompt on some devices.
- Fully silent transfer to every random PC is not guaranteed by public Android APIs.

## Build

Open this folder in Android Studio and run on a real Android device.

## Run flow

1. Install app and grant Bluetooth permissions.
2. Set default text payload in the app.
3. Pair Android with Ubuntu PC from Bluetooth settings.
4. When paired/connected, app auto-queues send attempt.
5. Accept receive prompt on Ubuntu if shown.
