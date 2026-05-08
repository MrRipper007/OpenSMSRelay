package io.github.opensmsrelay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.opensmsrelay.R
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.forwarding.ForwardingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class SmsForwardingService : Service() {

    @Inject
    lateinit var forwardingEngine: ForwardingEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeTasks = AtomicInteger(0)

    override fun onCreate() {
        super.onCreate()
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), foregroundServiceType)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sender = intent?.getStringExtra(EXTRA_SENDER)
        val body = intent?.getStringExtra(EXTRA_BODY)
        val receivedAt = intent?.getLongExtra(EXTRA_RECEIVED_AT, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        if (sender == null || body == null) {
            stopSelfWhenIdle()
            return START_NOT_STICKY
        }

        activeTasks.incrementAndGet()
        scope.launch {
            try {
                forwardingEngine.process(
                    SmsMessage(sender = sender, body = body, receivedAt = receivedAt)
                )
            } finally {
                if (activeTasks.decrementAndGet() == 0) stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun stopSelfWhenIdle() {
        if (activeTasks.get() == 0) stopSelf()
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_forwarding_text))
            .setSmallIcon(R.drawable.ic_notification_sms)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

    companion object {
        const val CHANNEL_ID = "sms_forwarding_channel"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_SENDER = "extra_sender"
        private const val EXTRA_BODY = "extra_body"
        private const val EXTRA_RECEIVED_AT = "extra_received_at"

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        fun startForMessage(context: Context, message: SmsMessage) {
            context.startForegroundService(
                Intent(context, SmsForwardingService::class.java).apply {
                    putExtra(EXTRA_SENDER, message.sender)
                    putExtra(EXTRA_BODY, message.body)
                    putExtra(EXTRA_RECEIVED_AT, message.receivedAt)
                }
            )
        }
    }
}
