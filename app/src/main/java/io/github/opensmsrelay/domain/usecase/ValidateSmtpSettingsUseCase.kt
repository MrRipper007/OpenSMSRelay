package io.github.opensmsrelay.domain.usecase

import io.github.opensmsrelay.core.common.Result
import io.github.opensmsrelay.core.common.isValidEmail
import io.github.opensmsrelay.domain.model.EmailSettings
import javax.inject.Inject

class ValidateSmtpSettingsUseCase @Inject constructor() {
    operator fun invoke(settings: EmailSettings): Result<Unit> {
        if (settings.host.isBlank()) return Result.Error("SMTP host is required")
        if (settings.port !in 1..65535) return Result.Error("Port must be between 1 and 65535")
        if (settings.username.isBlank()) return Result.Error("Username is required")
        if (settings.password.isBlank()) return Result.Error("Password is required")
        if (settings.fromEmail.isBlank()) return Result.Error("From email is required")
        if (!settings.fromEmail.isValidEmail()) return Result.Error("From email is not valid")
        return Result.Success(Unit)
    }
}
