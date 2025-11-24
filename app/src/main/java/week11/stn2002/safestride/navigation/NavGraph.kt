package week11.stn2002.safestride.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import week11.stn2002.safestride.ui.screens.auth.ForgotPasswordScreen
import week11.stn2002.safestride.ui.screens.auth.LoginScreen
import week11.stn2002.safestride.ui.screens.auth.RegisterScreen
import week11.stn2002.safestride.ui.screens.main.AlertHistoryScreen
import week11.stn2002.safestride.ui.screens.main.DashboardScreen
import week11.stn2002.safestride.ui.screens.main.ProfileScreen
import week11.stn2002.safestride.ui.viewmodel.AlertViewModel
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    alertViewModel: AlertViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Main Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                alertViewModel = alertViewModel
            )
        }

        composable(Screen.AlertHistory.route) {
            AlertHistoryScreen(
                navController = navController,
                alertViewModel = alertViewModel
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
