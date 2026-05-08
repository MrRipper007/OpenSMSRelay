package io.github.opensmsrelay.feature.logs

enum class LogFilter(val label: String) {
    ALL("All"),
    MATCHED("Matched"),
    IGNORED("Ignored"),
    FAILED("Failed")
}
