package io.github.opensmsrelay.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class AppPermissionState(
    val hasReceiveSms: Boolean,
    val hasSendSms: Boolean
) {
    val allGranted: Boolean get() = hasReceiveSms && hasSendSms
    val hasAny: Boolean get() = hasReceiveSms || hasSendSms
}

fun Context.getPermissionState(): AppPermissionState = AppPermissionState(
    hasReceiveSms = ContextCompat.checkSelfPermission(
        this, Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED,
    hasSendSms = ContextCompat.checkSelfPermission(
        this, Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED
)
