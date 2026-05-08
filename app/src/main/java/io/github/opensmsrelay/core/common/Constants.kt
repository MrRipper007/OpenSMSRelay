package io.github.opensmsrelay.core.common

object Constants {
    const val DATABASE_NAME = "opensmsrelay.db"
    const val FILE_PROVIDER_AUTHORITY = "io.github.opensmsrelay.fileprovider"
    const val MAX_EMAIL_RETRY_ATTEMPTS = 4
    const val CSV_EXPORT_FILENAME = "opensmsrelay_logs.csv"
    const val SMS_FORWARD_TEMPLATE = "Forwarded SMS\nFrom: {sender}\nTime: {time}\n\n{message}"
    const val EMAIL_SUBJECT_TEMPLATE = "SMS from {sender} at {time}"
}
