package io.github.opensmsrelay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.service.SmsForwardingService

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val receivedAt = System.currentTimeMillis()
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        smsMessages
            .groupBy { it.originatingAddress ?: "" }
            .forEach { (address, parts) ->
                SmsForwardingService.startForMessage(
                    context,
                    SmsMessage(
                        sender = address,
                        body = parts.joinToString("") { it.messageBody ?: "" },
                        receivedAt = receivedAt
                    )
                )
            }
    }
}
