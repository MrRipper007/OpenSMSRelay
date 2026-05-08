# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 1.x | Yes |

---

## Reporting a Vulnerability

If you discover a security vulnerability in Open SMS Relay, please report it **privately** before disclosing publicly.

**Email:** security@opensmsrelay.example (replace with actual contact)

**GitHub:** Use [GitHub Private Security Advisories](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability)

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Android version and device model (if relevant)

We aim to respond within 72 hours and publish a fix within 14 days for critical issues.

---

## Security Design

### Password Storage
SMTP password is stored using `EncryptedSharedPreferences` with `AES256_GCM` master key and `AES256_SIV` key encryption. It is never logged or stored in plaintext.

### SMS Receiver Protection
`SmsReceiver` is declared with `android:permission="android.permission.BROADCAST_SMS"`, ensuring only the telephony system can broadcast to it. Third-party apps cannot inject fake SMS intents.

### No Network Calls Beyond SMTP
The app's INTERNET permission is used exclusively for SMTP email sending. There are no analytics, CDN, or other outbound network requests.

### Minimum Permissions
- `RECEIVE_SMS`: for intercepting SMS (no `READ_SMS`)
- `SEND_SMS`: for forwarding to phone numbers
- `INTERNET`: for SMTP email only

### Local-Only Storage
All data (logs, rules, settings) is stored in the app's private data directory. `android:allowBackup="false"` prevents cloud backup of sensitive data.

### WorkManager
Email retry uses WorkManager with `CONNECTED` network constraint. No data is sent until a network connection is available.

---

## Known Risks

1. **SMTP credentials in memory**: The SMTP password is briefly held in memory during email sending. This is unavoidable for SMTP authentication.

2. **SMS interception by other apps**: If the user has another default SMS app with higher priority, messages may not be received. The receiver uses `android:priority="999"` to maximize receipt priority.

3. **Log retention**: Logs are stored indefinitely unless cleared by the user. Users with privacy concerns should use Privacy Mode and clear logs regularly.

4. **OTP masking is best-effort**: The OTP masking regex targets 4-8 digit standalone numbers. Unusual OTP formats may not be masked.

---

## Safe Usage Guidance

- Use a dedicated Gmail account with App Password for SMTP; do not use your primary account password
- Enable OTP Masking if forwarding banking messages
- Enable Privacy Mode if others may have access to your phone
- Review and delete logs regularly
- Only forward to email addresses and phone numbers you control
- Disable forwarding when not needed
