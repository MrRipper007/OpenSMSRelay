# Changelog

All notable changes to Open SMS Relay are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [1.0.0] - 2025-05-08

### Added
- Dashboard with forwarding toggle, permission status, active rule count, last SMS, and last forwarding result
- Forwarding rules with sender exact/contains/regex matching and optional body keyword filter
- Email forwarding via SMTP (SSL/TLS, STARTTLS, None) with configurable recipients
- SMS forwarding via Android SmsManager with multipart support
- Local Room database for logs with all forwarding statuses
- Log filtering: All, Matched, Ignored, Failed
- CSV log export via share sheet
- WorkManager email retry with exponential backoff
- Manual retry button for failed emails
- OTP masking: replaces 4-8 digit codes with `******` before forwarding
- Privacy Mode: hides message bodies in the logs UI
- SMTP password stored using EncryptedSharedPreferences (AES-256)
- Permission flow with rationale banners on Dashboard
- Security Settings screen with privacy warning
- Test Email and Test SMS buttons
- Clear logs with confirmation dialog
- README, PRIVACY, SECURITY, CONTRIBUTING documentation
- GitHub Actions CI: builds debug APK and runs unit tests on push/PR
- GPLv3 license

### Architecture
- Clean Architecture with MVVM
- Jetpack Compose + Material 3 UI
- Hilt dependency injection
- KSP annotation processing for Room and Hilt
- DataStore Preferences for non-sensitive settings
- Single-activity, NavGraph-based navigation
