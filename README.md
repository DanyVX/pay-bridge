# PayBridge

An Android app that lets a shopkeeper confirm a NayaPay/Easypaisa/JazzCash payment actually
landed — by reading the shopkeeper's own phone's notification stream — instead of trusting a
customer's screenshot. See the original project brief for the full rationale; this README covers
build/run/test instructions and current implementation status.

## Building

```
./gradlew assembleDebug   # debug build, installable directly
./gradlew testDebugUnitTest       # unit tests (matching engine, parser stubs, repository)
./gradlew connectedAndroidTest    # instrumented Room DAO tests (needs a connected device/emulator)
```

Requires a local Android SDK (set `sdk.dir` in `local.properties`, or have `ANDROID_HOME` set) —
this was developed and committed from an environment without SDK/emulator access, so `assembleDebug`
and the test suites have **not** been executed end-to-end yet. Do that first thing when you open
this in Android Studio, before trusting anything else in this README.

minSdk 26 (Android 8.0) / compileSdk & targetSdk 34. Not submitted to Google Play — see
"Distribution" below.

## Release signing (self-signed, direct APK)

PayBridge is distributed as a directly-installed APK, not through Google Play (see the project
brief for why). Release builds are signed with a self-signed keystore that is **never** committed.

1. Create a keystore once:
   ```
   keytool -genkeypair -v -keystore paybridge-release.jks -alias paybridge \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Store it outside the repo (or in a gitignored path inside it — `*.jks`/`*.keystore` are
   already gitignored).
3. Add these four lines to your local (gitignored) `local.properties`:
   ```
   PAYBRIDGE_KEYSTORE_PATH=/absolute/path/to/paybridge-release.jks
   PAYBRIDGE_KEYSTORE_PASSWORD=...
   PAYBRIDGE_KEY_ALIAS=paybridge
   PAYBRIDGE_KEY_PASSWORD=...
   ```
   (The same four keys also work as environment variables, for a future CI setup.)
4. `./gradlew assembleRelease` now produces a signed APK at
   `app/build/outputs/apk/release/app-release.apk`. Without a keystore configured, release builds
   still assemble (useful for local testing) but are left unsigned.

## Current status

**Real device/notification-format work not yet done — this is the honest starting point, not a
finished product.** Specifically:

- The three parsers (`NayaPayParser`, `EasypaisaParser`, `JazzCashParser`) are stubs that throw
  `NotImplementedError`. Nobody has captured real notification text from these apps yet — see
  `/docs/notification-samples.md` for the template to fill in, and do that before writing any
  parsing logic. Until then, every real payment notification correctly shows up as "unparsed"
  in the warning banner — that is expected, not a bug.
- The three payment-app package names in `PaymentAppAllowlist.kt` are **placeholders**, marked
  `// TODO(human): verify real package name`. Confirm them with
  `adb shell pm list packages | grep -i <name>` on a device that actually has these apps installed.

**Built and tested against mock fixtures (never real provider data):**

- Notification listener plumbing, package allowlist filtering, and the full ingestion pipeline
  (dedupe, unparsed-notification logging).
- The matching engine: exact-amount matching, provider scoping (including provider-less "match
  any" claims), oldest-pending-first ambiguity resolution with an on-screen note, the
  wrong-amount MISMATCH heuristic (only resolved when exactly one claim is in scope), and
  timeout resolution that distinguishes TIMED_OUT from COULD_NOT_VERIFY based on listener
  heartbeat history.
- Process-death safety: pending claims and incoming notifications live in Room, not just memory.
- Listener-down detection: checked on every app resume, on the listener's own connect/disconnect
  callbacks, and via a 15-minute WorkManager backstop for while the app is closed.
- Full UI flow: first-launch trust screen, notification-access permission flow with a persistent
  ACTIVE/NOT RUNNING banner, new-claim entry, the stamp-badge result screen, and history.

**Open edge case, not fully closed:** the WorkManager heartbeat backstop only narrows — it can't
fully close — the blind spot where the listener dies while the app is closed the whole time a
claim is open; WorkManager's 15-minute periodic floor is coarser than a 5-10 minute claim
timeout. See `MatchingEngine`'s doc comments and the plan history for the reasoning.

## Testing note — real hardware validation is still required

Unit tests here only prove the matching/timeout/dedupe logic is correct against known inputs.
The single biggest reliability risk for this app — OEM background-kill behavior on phones common
in Pakistan (Xiaomi/MIUI, Vivo, Oppo, and others) — is device-specific and cannot be validated on
an emulator or with a fresh install alone. **Before this is trusted for a real transaction, it
needs to be tested on the actual phone brand/model the pilot shopkeeper (Madina Electronics)
uses, across a real reboot and a real period of normal day-to-day phone usage.**

## Docs

- `/docs/notification-samples.md` — template for capturing real NayaPay/Easypaisa/JazzCash
  notification text; fill this in before writing any parser logic.
- `/docs/INSTALL_GUIDE.md` — plain-language sideload instructions for a non-technical shopkeeper.

## Next step

1. Capture real notification samples (`/docs/notification-samples.md`) from a device with all
   three apps installed.
2. Verify the real package names in `PaymentAppAllowlist.kt`.
3. Implement the three parsers against those real samples and un-skip their `@Ignore`d tests.
4. Run the full test suite and `assembleDebug` for the first time in a real Android Studio/SDK
   environment (not yet done from this session).
5. Sideload onto a real test phone and validate across a reboot before trusting it for a real
   transaction.
