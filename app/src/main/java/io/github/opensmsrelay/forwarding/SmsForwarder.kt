package io.github.opensmsrelay.forwarding

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.opensmsrelay.core.common.Constants
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.core.common.toReadableDateTime
import io.github.opensmsrelay.domain.model.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class SmsForwarder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun send(
        smsMessage: SmsMessage,
        destinations: List<String>,
        maskedBody: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (destinations.isEmpty()) return@withContext Result.Error("No destination numbers configured")

        runCatching {
            val text = Constants.SMS_FORWARD_TEMPLATE
                .replace("{sender}", smsMessage.sender)
                .replace("{time}", smsMessage.receivedAt.toReadableDateTime())
                .replace("{message}", maskedBody)

            val smsManager = getSmsManager()

            for (destination in destinations) {
                val timedOut = withTimeoutOrNull(30_000L) {
                    sendWithConfirmation(smsManager, destination, text)
                } == null
                if (timedOut) throw Exception("SMS send timed out for $destination")
            }
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Error(it.message ?: "SMS send failed", it) }
        )
    }

    private suspend fun sendWithConfirmation(
        smsManager: SmsManager,
        destination: String,
        text: String
    ) = suspendCancellableCoroutine<Unit> { cont ->
        val action = "io.github.opensmsrelay.SMS_SENT_${System.nanoTime()}"

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                runCatching { context.unregisterReceiver(this) }
                if (cont.isActive) {
                    when (resultCode) {
                        Activity.RESULT_OK -> cont.resume(Unit)
                        SmsManager.RESULT_ERROR_NO_SERVICE ->
                            cont.resumeWithException(Exception("No SMS service — check SIM/signal"))
                        SmsManager.RESULT_ERROR_RADIO_OFF ->
                            cont.resumeWithException(Exception("Mobile radio is off"))
                        SmsManager.RESULT_ERROR_NULL_PDU ->
                            cont.resumeWithException(Exception("SMS PDU error"))
                        else ->
                            cont.resumeWithException(Exception("SMS send failed (code $resultCode)"))
                    }
                }
            }
        }

        val filter = IntentFilter(action)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        cont.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(action),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching {
            val parts = smsManager.divideMessage(text)
            if (parts.size == 1) {
                smsManager.sendTextMessage(destination, null, text, pendingIntent, null)
            } else {
                // Track send result on the last part only
                val sentIntents = ArrayList<PendingIntent?>(parts.size).apply {
                    repeat(parts.size - 1) { add(null) }
                    add(pendingIntent)
                }
                smsManager.sendMultipartTextMessage(destination, null, parts, sentIntents, null)
            }
        }.onFailure { ex ->
            runCatching { context.unregisterReceiver(receiver) }
            if (cont.isActive) cont.resumeWithException(ex)
        }
    }

    @Suppress("DEPRECATION")
    private fun getSmsManager(): SmsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
}
