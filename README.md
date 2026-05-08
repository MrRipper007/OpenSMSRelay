# Open SMS Relay

A transparent, consent-based SMS forwarding utility for Android.

[![Build](https://github.com/opensmsrelay/OpenSMSRelay/actions/workflows/android-build.yml/badge.svg)](https://github.com/opensmsrelay/OpenSMSRelay/actions)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

---

## What it does

Open SMS Relay receives incoming SMS messages and forwards matched messages to:

- **Email** — via user-configured SMTP (e.g. Gmail with App Password)
- **Other phone numbers** — via the device SIM using Android's SmsManager

Forwarding only happens when you explicitly create a rule. Nothing is forwarded by default.

---

## What it does NOT do

- Does **not** support RCS, WhatsApp, Messenger, Telegram, Viber, or any other encrypted or internet-based messaging platform. **SMS only.**
- Does **not** send any data to any developer-controlled server.
- Does **not** collect analytics, crash reports, or telemetry.
- Does **not** show ads.
- Does **not** run hidden background services beyond what is needed for SMS reception.
- Does **not** forward SMS unless you create a rule.
- Does **not** store your SMTP password in plaintext.

---

## Features

- **Dashboard** — forwarding on/off toggle, permission status, active rule count, last SMS, last result
- **Rules** — define which senders to forward; match by exact, contains, or regex; optional body keyword filter
- **Email forwarding** — SMTP with SSL/TLS or STARTTLS; configurable recipients; retry on failure
- **SMS forwarding** — forward to other phone numbers via SIM; multipart SMS support
- **Logs** — local-only history of every SMS received; filterable by matched/ignored/failed; CSV export
- **Security** — optional OTP masking (replaces 4-8 digit codes with `******`); Privacy Mode hides bodies in logs; no plaintext password storage
- **WorkManager retry** — failed email forwards are retried with exponential backoff

---

## Permissions

| Permission | Why it is needed |
|---|---|
| `RECEIVE_SMS` | To intercept incoming SMS messages for rule matching |
| `SEND_SMS` | To forward matched SMS messages to other phone numbers |
| `INTERNET` | To send forwarded messages via SMTP email |

**Not requested:** `READ_SMS`, `READ_CONTACTS`, `READ_PHONE_STATE`, location, camera, microphone.

---

## Privacy Promise

- All data stays on your device.
- Logs are stored in a local Room database.
- SMTP password is stored using Android's `EncryptedSharedPreferences`.
- No analytics. No crash reporting. No developer server. No cloud backup of sensitive data.

**Warning:** Forwarding SMS can expose private, banking, or OTP messages. Only forward to destinations you fully control.

---

## Gmail SMTP App Password Setup

1. Go to your Google Account → **Security**
2. Enable **2-Step Verification** (required for App Passwords)
3. Go to **Security → App Passwords**
4. Create an app password (e.g. "Open SMS Relay")
5. Use `smtp.gmail.com`, port `587`, security `STARTTLS`
6. Use your Gmail address as username and the 16-character app password

---

## Build from Source

### Requirements
- Android Studio Meerkat (2024.3+) or newer
- JDK 17
- Android SDK 26+

### Steps

```bash
git clone https://github.com/opensmsrelay/OpenSMSRelay.git
cd OpenSMSRelay

# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Tests

```bash
./gradlew test
```

---

## Install APK

1. Download the APK from [Releases](https://github.com/opensmsrelay/OpenSMSRelay/releases)
2. Verify the SHA256 checksum listed in the release notes
3. Enable "Install from unknown sources" in Android Settings → Security
4. Open the APK file on your device to install

---

## Architecture

```
app/src/main/java/io/github/opensmsrelay/
├── core/           common utilities, security, navigation, design system
├── data/           Room database, DataStore, repository implementations
├── domain/         models, repository interfaces, use cases
├── feature/        Compose UI screens and ViewModels
├── receiver/       SmsReceiver (BroadcastReceiver)
├── forwarding/     ForwardingEngine, EmailForwarder, SmsForwarder
├── worker/         EmailRetryWorker (WorkManager)
└── di/             Hilt dependency injection modules
```

Tech stack: Kotlin · Jetpack Compose · Material 3 · Room · Hilt · WorkManager · DataStore · EncryptedSharedPreferences · JavaMail (android-mail)

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

GNU General Public License v3.0 — see [LICENSE](LICENSE).
