package io.github.opensmsrelay.domain.model

enum class ForwardingStatus {
    NOT_ATTEMPTED,
    PENDING,
    SUCCESS,
    FAILED,
    RETRYING,
    DISABLED
}
