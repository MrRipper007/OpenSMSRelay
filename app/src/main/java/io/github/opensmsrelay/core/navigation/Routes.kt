package io.github.opensmsrelay.core.navigation

sealed class Routes(val route: String) {
    object Dashboard : Routes("dashboard")
    object Rules : Routes("rules")
    object RuleEdit : Routes("rules/edit?ruleId={ruleId}") {
        fun createRoute(ruleId: Long? = null): String =
            if (ruleId != null) "rules/edit?ruleId=$ruleId" else "rules/edit"
    }
    object EmailSettings : Routes("email_settings")
    object SmsSettings : Routes("sms_settings")
    object Logs : Routes("logs")
    object SecuritySettings : Routes("security_settings")
}
