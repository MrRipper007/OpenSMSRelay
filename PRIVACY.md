# Privacy Policy

**Last updated:** 2025-05-08

Open SMS Relay is a local, consent-based utility. This document explains exactly what data the app handles and where it goes.

---

## Data collected and where it is stored

| Data | Stored where | Leaves device? |
|---|---|---|
| Forwarding rules | Local Room database | Never |
| SMS log entries | Local Room database | Never |
| SMTP host, port, username | Local DataStore (plaintext) | Never |
| SMTP password | EncryptedSharedPreferences (AES-256) | Never |
| Recipient emails / phone numbers | Local DataStore | Never |
| App settings | Local DataStore | Never |

---

## Data that leaves the device

The only data that leaves the device is the **SMS content itself**, sent to destinations **you configure**:

- **Email**: sent to the SMTP server you configure (e.g. Gmail)
- **SMS**: sent to the phone numbers you configure via the device SIM

**No data is sent to any Open SMS Relay server, developer, or third party.**

---

## What the app does NOT do

- No analytics
- No crash reporting
- No telemetry
- No advertising network
- No developer-controlled backend
- No automatic cloud backup of SMS content

---

## Security warning

Forwarding SMS messages can expose:

- Banking OTPs and one-time codes
- Login verification codes
- Private conversations

**Only configure forwarding to destinations you trust and control.** Enable OTP Masking in Security Settings to reduce exposure of sensitive codes.

---

## Your rights

You can:

- View all logs in the Logs screen
- Clear all logs at any time
- Export logs as CSV
- Delete all rules
- Disable forwarding globally
- Clear SMTP credentials

---

## Open Source

This application is fully open source under the GNU GPLv3 license. Anyone can audit the code at:

https://github.com/opensmsrelay/OpenSMSRelay
