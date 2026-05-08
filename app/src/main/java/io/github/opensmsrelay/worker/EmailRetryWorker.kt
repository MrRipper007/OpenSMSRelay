package io.github.opensmsrelay.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.opensmsrelay.core.common.Constants
import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SenderMatchType
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.domain.repository.EmailSettingsRepository
import io.github.opensmsrelay.domain.repository.LogRepository
import io.github.opensmsrelay.domain.repository.RuleRepository
import io.github.opensmsrelay.forwarding.EmailForwarder

@HiltWorker
class EmailRetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val logRepository: LogRepository,
    private val ruleRepository: RuleRepository,
    private val emailForwarder: EmailForwarder,
    private val emailSettingsRepository: EmailSettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val logId = inputData.getLong(KEY_LOG_ID, -1L)
        if (logId == -1L) return Result.failure()

        val log = logRepository.getById(logId) ?: return Result.failure()
        val emailSettings = emailSettingsRepository.get()
        val rule = log.matchedRuleId?.let { ruleRepository.getById(it) }

        val recipients = rule?.emailDestinations.orEmpty()
        if (!emailSettings.isEnabled || recipients.isEmpty()) return Result.failure()

        // Rebuild a minimal Rule placeholder for the email template
        val ruleStub = Rule(
            id = log.matchedRuleId ?: 0L,
            name = log.matchedRuleName ?: "Unknown Rule",
            matchType = SenderMatchType.EXACT,
            senderValue = log.sender
        )

        val smsMessage = SmsMessage(
            sender = log.sender,
            body = log.body,
            receivedAt = log.timestamp
        )

        return when (val result = emailForwarder.send(
            smsMessage,
            ruleStub,
            emailSettings.copy(recipients = recipients),
            log.body
        )) {
            is io.github.opensmsrelay.core.common.Result.Success -> {
                logRepository.update(
                    log.copy(
                        emailStatus = ForwardingStatus.SUCCESS,
                        retryCount = runAttemptCount,
                        errorMessage = null
                    )
                )
                Result.success()
            }
            is io.github.opensmsrelay.core.common.Result.Error -> {
                logRepository.update(
                    log.copy(
                        emailStatus = if (runAttemptCount >= Constants.MAX_EMAIL_RETRY_ATTEMPTS)
                            ForwardingStatus.FAILED else ForwardingStatus.RETRYING,
                        retryCount = runAttemptCount,
                        errorMessage = result.message
                    )
                )
                if (runAttemptCount >= Constants.MAX_EMAIL_RETRY_ATTEMPTS) {
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
            else -> Result.retry()
        }
    }

    companion object {
        const val KEY_LOG_ID = "log_id"
    }
}
