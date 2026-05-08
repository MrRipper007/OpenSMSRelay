# Contributing to Open SMS Relay

Thank you for considering a contribution. Please read this guide before submitting.

---

## Development Setup

### Requirements
- Android Studio Meerkat (2024.3+)
- JDK 17
- Android SDK 26+ (minSdk 26, compileSdk 36)
- A physical Android device for SMS testing (emulators cannot receive real SMS)

### Clone and Build

```bash
git clone https://github.com/opensmsrelay/OpenSMSRelay.git
cd OpenSMSRelay
./gradlew assembleDebug
```

### Run Tests

```bash
./gradlew test
```

---

## Architecture

The project uses Clean Architecture:

```
core/        shared utilities; no Android dependencies where possible
domain/      business logic: models, repository interfaces, use cases
data/        implementations: Room, DataStore, repository impls
feature/     Compose UI screens and HiltViewModels
forwarding/  SMS/email dispatch logic
receiver/    BroadcastReceiver for incoming SMS
worker/      WorkManager retry worker
di/          Hilt modules
```

**Do not put Android framework code in domain/.**
**Do not put business logic in feature/ screens.**

---

## Code Style

- Kotlin official style guide
- No wildcard imports
- Max line length: 120 characters
- Use `data class` for models; keep them immutable
- ViewModels expose `StateFlow`, not `LiveData`
- Coroutines on `Dispatchers.IO` for I/O; `Dispatchers.Main` is managed by Compose

---

## Commit Style

Use short imperative commit messages:

```
Add OTP masking unit test
Fix regex crash on invalid pattern
Update SMTP retry backoff to exponential
```

---

## Pull Request Checklist

- [ ] Code compiles: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew test`
- [ ] No new permissions added without justification
- [ ] No analytics or tracking code added
- [ ] No hardcoded email addresses or phone numbers
- [ ] No SMTP password logged or stored in plaintext
- [ ] Privacy warning text preserved in Security Settings
- [ ] CHANGELOG.md updated

---

## Hard Rules

These changes will be rejected:

- Adding analytics, crash reporting, or telemetry
- Sending data to any developer-controlled server
- Removing the privacy warning from Security Settings
- Storing SMTP password in plaintext
- Adding permissions not strictly needed
- Auto-starting hidden background services
- Hiding the app icon

---

## Reporting Issues

Please use [GitHub Issues](https://github.com/opensmsrelay/OpenSMSRelay/issues) for bugs and feature requests.

For security vulnerabilities, see [SECURITY.md](SECURITY.md).
