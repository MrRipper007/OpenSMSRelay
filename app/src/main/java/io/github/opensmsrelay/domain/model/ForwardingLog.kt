package io.github.opensmsrelay.domain.model

data class ForwardingLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String,
    val body: String,
    val matchedRuleId: Long? = null,
    val matchedRuleName: String? = null,
    val isMatched: Boolean = false,
    val emailStatus: ForwardingStatus = ForwardingStatus.NOT_ATTEMPTED,
    val smsStatus: ForwardingStatus = ForwardingStatus.NOT_ATTEMPTED,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
