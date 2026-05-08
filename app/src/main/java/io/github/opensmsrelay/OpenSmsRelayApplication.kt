package io.github.opensmsrelay

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.github.opensmsrelay.service.SmsForwardingService
import javax.inject.Inject

@HiltAndroidApp
class OpenSmsRelayApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        SmsForwardingService.createNotificationChannel(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.WARN)
            .build()
}
