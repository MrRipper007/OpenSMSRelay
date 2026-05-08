package io.github.opensmsrelay.data.mapper

import io.github.opensmsrelay.data.local.entity.RuleEntity
import io.github.opensmsrelay.domain.model.Rule

fun RuleEntity.toDomain(): Rule = Rule(
    id = id,
    name = name,
    isEnabled = isEnabled,
    matchType = matchType,
    senderValue = senderValue,
    bodyFilter = bodyFilter,
    forwardEmail = forwardEmail,
    forwardSms = forwardSms,
    emailDestinations = emailDestinations,
    smsDestinations = smsDestinations,
    createdAt = createdAt
)

fun Rule.toEntity(): RuleEntity = RuleEntity(
    id = id,
    name = name,
    isEnabled = isEnabled,
    matchType = matchType,
    senderValue = senderValue,
    bodyFilter = bodyFilter,
    forwardEmail = forwardEmail,
    forwardSms = forwardSms,
    emailDestinations = emailDestinations,
    smsDestinations = smsDestinations,
    createdAt = createdAt
)
