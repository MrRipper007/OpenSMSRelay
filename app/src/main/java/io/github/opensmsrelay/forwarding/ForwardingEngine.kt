package io.github.opensmsrelay.forwarding

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.core.security.OtpMasker
import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.domain.repository.AppSettingsRepository
import io.github.opensmsrelay.domain.repository.EmailSettingsRepository
import io.github.opensmsrelay.domain.repository.LogRepository
import io.github.opensmsrelay.domain.repository.SmsSettingsRepository
import io.github.opensmsrelay.domain.usecase.MatchRulesUseCase
import io.github.opensmsrelay.worker.EmailRetryWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwardingEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matchRulesUseCase: MatchRulesUseCase,
    private val logRepository: LogRepository,
    private val emailForwarder: EmailForwarder,
    private val smsForwarder: SmsForwarder,
    private val emailSettingsRepository: EmailSettingsRepository,
    private val smsSettingsRepository: SmsSettingsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend fun process(smsMessage: SmsMessage) {
        val appSettings = appSettingsRepository.get()

        if (!appSettings.forwardingEnabled) {
            val safeBody = when {
                appSettings.privacyMode -> "[hidden]"
                appSettings.otpMasking -> OtpMasker.maskIfEnabled(smsMessage.body, true)
                else -> smsMessage.body
            }
            logRepository.insert(
                ForwardingLog(
                    sender = smsMessage.sender,
                    body = safeBody,
                    timestamp = smsMessage.receivedAt,
                    isMatched = false,
                    emailStatus = ForwardingStatus.DISABLED,
                    smsStatus = ForwardingStatus.DISABLED
                )
            )
            return
        }

        val matchedRules = matchRulesUseCase(smsMessage)
        val maskedBody = OtpMasker.maskIfEnabled(smsMessage.body, appSettings.otpMasking)
        val logBody = when {
            appSettings.privacyMode -> "[hidden]"
            appSettings.otpMasking -> maskedBody  // never persist raw OTPs
            else -> smsMessage.body
        }

        if (matchedRules.isEmpty()) {
            logRepository.insert(
                ForwardingLog(
                    sender = smsMessage.sender,
                    body = logBody,
                    timestamp = smsMessage.receivedAt,
                    isMatched = false,
                    emailStatus = ForwardingStatus.NOT_ATTEMPTED,
                    smsStatus = ForwardingStatus.NOT_ATTEMPTED
                )
            )
            return
        }

        // Process each matched rule (for MVP, process all matches)
        for (rule in matchedRules) {
            processRule(smsMessage, rule, maskedBody, logBody, appSettings.privacyMode)
        }
    }

    private suspend fun processRule(
        smsMessage: SmsMessage,
        rule: Rule,
        maskedBody: String,
        logBody: String,
        privacyMode: Boolean
    ) {
        val emailSettings = emailSettingsRepository.get()
        val smsSettings = smsSettingsRepository.get()

        val effectiveSmsDestinations = rule.smsDestinations.ifEmpty { smsSettings.destinations }

        var emailStatus = ForwardingStatus.NOT_ATTEMPTED
        var smsStatus = ForwardingStatus.NOT_ATTEMPTED
        var errorMessage: String? = null

        if (rule.forwardEmail) {
            if (!emailSettings.isEnabled || rule.emailDestinations.isEmpty()) {
                emailStatus = ForwardingStatus.DISABLED
            } else {
                emailStatus = ForwardingStatus.PENDING
                when (val result = emailForwarder.send(
                    smsMessage, rule,
                    emailSettings.copy(recipients = rule.emailDestinations),
                    maskedBody
                )) {
                    is Result.Success -> emailStatus = ForwardingStatus.SUCCESS
                    is Result.Error -> {
                        emailStatus = ForwardingStatus.FAILED
                        errorMessage = result.message
                    }
                    else -> {}
                }
            }
        }

        // SMS forwarding
        if (rule.forwardSms) {
            if (effectiveSmsDestinations.isEmpty()) {
                smsStatus = ForwardingStatus.DISABLED
            } else {
                smsStatus = ForwardingStatus.PENDING
                when (val result = smsForwarder.send(smsMessage, effectiveSmsDestinations, maskedBody)) {
                    is Result.Success -> smsStatus = ForwardingStatus.SUCCESS
                    is Result.Error -> {
                        smsStatus = ForwardingStatus.FAILED
                        if (errorMessage == null) errorMessage = result.message
                    }
                    else -> {}
                }
            }
        }

        val logId = logRepository.insert(
            ForwardingLog(
                sender = smsMessage.sender,
                body = logBody,
                timestamp = smsMessage.receivedAt,
                matchedRuleId = rule.id,
                matchedRuleName = rule.name,
                isMatched = true,
                emailStatus = emailStatus,
                smsStatus = smsStatus,
                errorMessage = errorMessage
            )
        )

        // Queue retry if email failed
        if (emailStatus == ForwardingStatus.FAILED) {
            enqueueEmailRetry(logId)
        }
    }

    private fun enqueueEmailRetry(logId: Long) {
        val request = OneTimeWorkRequestBuilder<EmailRetryWorker>()
            .setInputData(workDataOf(EmailRetryWorker.KEY_LOG_ID to logId))
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .addTag("email_retry_$logId")
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
