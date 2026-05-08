package io.github.opensmsrelay.domain.usecase

import io.github.opensmsrelay.core.common.csvEscape
import io.github.opensmsrelay.core.common.toReadableDateTime
import io.github.opensmsrelay.domain.repository.LogRepository
import javax.inject.Inject

class ExportLogsUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(): String {
        val logs = logRepository.getAll()
        val header = "ID,Timestamp,Sender,Body,Matched Rule,Email Status,SMS Status,Retry Count,Error\n"
        val rows = logs.joinToString("\n") { log ->
            listOf(
                log.id.toString(),
                log.timestamp.toReadableDateTime().csvEscape(),
                log.sender.csvEscape(),
                log.body.csvEscape(),
                (log.matchedRuleName ?: "").csvEscape(),
                log.emailStatus.name,
                log.smsStatus.name,
                log.retryCount.toString(),
                (log.errorMessage ?: "").csvEscape()
            ).joinToString(",")
        }
        return header + rows
    }
}
