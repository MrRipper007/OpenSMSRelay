package io.github.opensmsrelay.domain.usecase

import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.model.SenderMatchType
import io.github.opensmsrelay.domain.model.SmsMessage
import io.github.opensmsrelay.domain.repository.RuleRepository
import javax.inject.Inject

class MatchRulesUseCase @Inject constructor(
    private val ruleRepository: RuleRepository
) {
    suspend operator fun invoke(message: SmsMessage): List<Rule> {
        val rules = ruleRepository.getAll()
        return rules.filter { rule ->
            rule.isEnabled &&
                senderMatches(rule, message.sender) &&
                bodyMatches(rule, message.body)
        }
    }

    private fun senderMatches(rule: Rule, sender: String): Boolean = when (rule.matchType) {
        SenderMatchType.EXACT -> sender.equals(rule.senderValue.trim(), ignoreCase = true)
        SenderMatchType.CONTAINS -> sender.contains(rule.senderValue.trim(), ignoreCase = true)
        SenderMatchType.REGEX -> runCatching {
            Regex(rule.senderValue, RegexOption.IGNORE_CASE).containsMatchIn(sender)
        }.getOrDefault(false)
    }

    private fun bodyMatches(rule: Rule, body: String): Boolean {
        val filter = rule.bodyFilter?.trim()
        return filter.isNullOrEmpty() || body.contains(filter, ignoreCase = true)
    }
}
