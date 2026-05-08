package io.github.opensmsrelay.data.mapper

import io.github.opensmsrelay.data.local.entity.ForwardingLogEntity
import io.github.opensmsrelay.domain.model.ForwardingLog

fun ForwardingLogEntity.toDomain(): ForwardingLog = ForwardingLog(
    id = id,
    timestamp = timestamp,
    sender = sender,
    body = body,
    matchedRuleId = matchedRuleId,
    matchedRuleName = matchedRuleName,
    isMatched = isMatched,
    emailStatus = emailStatus,
    smsStatus = smsStatus,
    retryCount = retryCount,
    errorMessage = errorMessage
)

fun ForwardingLog.toEntity(): ForwardingLogEntity = ForwardingLogEntity(
    id = id,
    timestamp = timestamp,
    sender = sender,
    body = body,
    matchedRuleId = matchedRuleId,
    matchedRuleName = matchedRuleName,
    isMatched = isMatched,
    emailStatus = emailStatus,
    smsStatus = smsStatus,
    retryCount = retryCount,
    errorMessage = errorMessage
)
