package io.github.opensmsrelay.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.opensmsrelay.feature.dashboard.DashboardScreen
import io.github.opensmsrelay.feature.emailsettings.EmailSettingsScreen
import io.github.opensmsrelay.feature.logs.LogsScreen
import io.github.opensmsrelay.feature.rules.RuleEditScreen
import io.github.opensmsrelay.feature.rules.RuleListScreen
import io.github.opensmsrelay.feature.securitysettings.SecuritySettingsScreen
import io.github.opensmsrelay.feature.smssettings.SmsSettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route
    ) {
        composable(Routes.Dashboard.route) {
            DashboardScreen(
                onNavigateToRules = { navController.navigate(Routes.Rules.route) },
                onNavigateToEmailSettings = { navController.navigate(Routes.EmailSettings.route) },
                onNavigateToSmsSettings = { navController.navigate(Routes.SmsSettings.route) },
                onNavigateToLogs = { navController.navigate(Routes.Logs.route) },
                onNavigateToSecurity = { navController.navigate(Routes.SecuritySettings.route) }
            )
        }
        composable(Routes.Rules.route) {
            RuleListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddRule = { navController.navigate(Routes.RuleEdit.createRoute()) },
                onEditRule = { id -> navController.navigate(Routes.RuleEdit.createRoute(id)) }
            )
        }
        composable(
            route = Routes.RuleEdit.route,
            arguments = listOf(
                navArgument("ruleId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            RuleEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EmailSettings.route) {
            EmailSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SmsSettings.route) {
            SmsSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.Logs.route) {
            LogsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SecuritySettings.route) {
            SecuritySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
