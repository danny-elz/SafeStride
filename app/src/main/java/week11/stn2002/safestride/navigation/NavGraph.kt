package week11.stn2002.safestride.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import week11.stn2002.safestride.ui.components.FallDetectionDialog
import week11.stn2002.safestride.ui.screens.SplashScreen
import week11.stn2002.safestride.ui.screens.auth.ForgotPasswordScreen
import week11.stn2002.safestride.ui.screens.auth.LoginScreen
import week11.stn2002.safestride.ui.screens.auth.RegisterScreen
import week11.stn2002.safestride.ui.screens.main.AlertHistoryScreen
import week11.stn2002.safestride.ui.screens.main.DashboardScreen
import week11.stn2002.safestride.ui.screens.main.ProfileScreen
import week11.stn2002.safestride.ui.screens.main.SafeWalkScreen
import week11.stn2002.safestride.ui.viewmodel.AlertViewModel
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel
import week11.stn2002.safestride.ui.viewmodel.SafeWalkViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    alertViewModel: AlertViewModel,
    safeWalkViewModel: SafeWalkViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    val destination = if (authViewModel.isUserLoggedIn) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
                alertViewModel = alertViewModel,
                safeWalkViewModel = safeWalkViewModel
            )
        }

        composable(Screen.SafeWalk.route) {
            val elapsedTime by safeWalkViewModel.elapsedTime.collectAsState()
            val fallDetected by safeWalkViewModel.fallDetected.collectAsState()
            val currentLocation by safeWalkViewModel.currentLocation.collectAsState()
            val isRunning by safeWalkViewModel.isRunning.collectAsState()

            var showFallDialog by remember { mutableStateOf(false) }
            var showSOSConfirmation by remember { mutableStateOf(false) }

            // keep checking the service for updates while we're on this screen
            LaunchedEffect(Unit) {
                while (true) {
                    safeWalkViewModel.updateFromService()
                    delay(500)
                }
            }

            // pop up the "are you okay?" dialog when a fall is detected
            LaunchedEffect(fallDetected) {
                if (fallDetected) {
                    Log.w("NavGraph", "Fall detected on SafeWalk screen - showing dialog")
                    showFallDialog = true
                }
            }

            SafeWalkScreen(
                navController = navController,
                elapsedTime = elapsedTime,
                onStopSafeWalk = {
                    Log.d("NavGraph", "User stopped Safe Walk")
                    safeWalkViewModel.stopSafeWalk()
                    navController.popBackStack()
                },
                onEmergencySOS = {
                    Log.w("NavGraph", "MANUAL SOS triggered from SafeWalk screen at lat=${currentLocation?.latitude}, lng=${currentLocation?.longitude}")
                    alertViewModel.createManualAlert(
                        latitude = currentLocation?.latitude ?: 0.0,
                        longitude = currentLocation?.longitude ?: 0.0,
                        address = "Emergency SOS during Safe Walk"
                    )
                    showSOSConfirmation = true
                }
            )

            // the "are you okay?" dialog with countdown timer
            if (showFallDialog) {
                FallDetectionDialog(
                    onDismiss = {
                        Log.d("NavGraph", "Fall dialog dismissed")
                        showFallDialog = false
                        safeWalkViewModel.resetFallDetection()
                    },
                    onImOkay = {
                        Log.d("NavGraph", "User pressed I'm OK")
                        safeWalkViewModel.resetFallDetection()
                    },
                    onSendSOS = {
                        Log.w("NavGraph", "AUTOMATIC SOS triggered from fall detection at lat=${currentLocation?.latitude}, lng=${currentLocation?.longitude}")
                        alertViewModel.createAutomaticAlert(
                            latitude = currentLocation?.latitude ?: 0.0,
                            longitude = currentLocation?.longitude ?: 0.0,
                            address = "Automatic alert - Fall detected during Safe Walk"
                        )
                    }
                )
            }

            // shows a quick confirmation after SOS is sent
            if (showSOSConfirmation) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showSOSConfirmation = false },
                    title = { androidx.compose.material3.Text("SOS Alert Sent") },
                    text = { androidx.compose.material3.Text("Your emergency SOS alert has been created and saved.") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showSOSConfirmation = false }) {
                            androidx.compose.material3.Text("OK")
                        }
                    }
                )
            }
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
