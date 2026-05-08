package io.github.opensmsrelay.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toReadableDateTime(): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(Date(this))
}

fun String.truncate(maxLength: Int, suffix: String = "…"): String =
    if (length > maxLength) take(maxLength) + suffix else this

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidSmtpPort(): Boolean =
    toIntOrNull()?.let { it in 1..65535 } ?: false

fun String.parseEmailList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun String.parsePhoneList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun String.csvEscape(): String = "\"${replace("\"", "\"\"")}\""
