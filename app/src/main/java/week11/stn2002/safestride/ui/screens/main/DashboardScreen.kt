package week11.stn2002.safestride.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import week11.stn2002.safestride.navigation.Screen
import week11.stn2002.safestride.ui.components.FallDetectionDialog
import week11.stn2002.safestride.ui.theme.AlertRed
import week11.stn2002.safestride.ui.theme.SafeBlue40
import week11.stn2002.safestride.ui.viewmodel.AlertViewModel
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel
import week11.stn2002.safestride.ui.viewmodel.SafeWalkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    alertViewModel: AlertViewModel,
    safeWalkViewModel: SafeWalkViewModel
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSOSConfirmation by remember { mutableStateOf(false) }
    var showFallDetectionDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val createAlertState by alertViewModel.createAlertState.collectAsState()
    val isRunning by safeWalkViewModel.isRunning.collectAsState()
    val fallDetected by safeWalkViewModel.fallDetected.collectAsState()
    val elapsedTime by safeWalkViewModel.elapsedTime.collectAsState()
    val currentLocation by safeWalkViewModel.currentLocation.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            safeWalkViewModel.startSafeWalk(context)
            navController.navigate(Screen.SafeWalk.route)
        } else {
            showPermissionRationale = true
        }
    }

    // Check for required permissions
    fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && coarseLocation && notification
    }

    fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // show confirmation dialog when alert is successfully created
    LaunchedEffect(createAlertState) {
        if (createAlertState is week11.stn2002.safestride.util.Resource.Success) {
            showSOSConfirmation = true
            alertViewModel.resetCreateAlertState()
        }
    }

    // keep polling for fall detection while safe walk is running
    LaunchedEffect(isRunning) {
        while (isRunning) {
            safeWalkViewModel.updateFromService()
            delay(500)
        }
    }

    // pop up the fall dialog if sensors detect a potential fall
    LaunchedEffect(fallDetected) {
        if (fallDetected) {
            showFallDetectionDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeStride") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafeBlue40,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isRunning) {
                FloatingActionButton(
                    onClick = {
                        if (hasRequiredPermissions()) {
                            try {
                                safeWalkViewModel.startSafeWalk(context)
                            } catch (e: Exception) {
                                // Service might fail but still navigate
                                e.printStackTrace()
                            }
                            navController.navigate(Screen.SafeWalk.route)
                        } else {
                            requestPermissions()
                        }
                    },
                    containerColor = SafeBlue40,
                    contentColor = Color.White
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Start Safe Walk")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // big red panic button for emergencies
            Button(
                onClick = {
                    Log.w("DashboardScreen", "MANUAL SOS button pressed at lat=${currentLocation?.latitude}, lng=${currentLocation?.longitude}")
                    alertViewModel.createManualAlert(
                        latitude = currentLocation?.latitude ?: 0.0,
                        longitude = currentLocation?.longitude ?: 0.0,
                        address = "Manual SOS triggered from Dashboard"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRed
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SOS ALERT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // takes user to see their past alerts
            OutlinedButton(
                onClick = { navController.navigate(Screen.AlertHistory.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SafeBlue40
                )
            ) {
                Icon(Icons.Default.History, contentDescription = "History")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Alert History")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // starts the safe walk mode with fall detection
            Button(
                onClick = {
                    if (hasRequiredPermissions()) {
                        try {
                            safeWalkViewModel.startSafeWalk(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        navController.navigate(Screen.SafeWalk.route)
                    } else {
                        requestPermissions()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeBlue40
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Walk")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Safe Walk", fontWeight = FontWeight.Medium)
            }
        }

        // asks user to confirm before logging out
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (isRunning) {
                                safeWalkViewModel.stopSafeWalk()
                            }
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // lets user know their SOS was sent successfully
        if (showSOSConfirmation) {
            AlertDialog(
                onDismissRequest = { showSOSConfirmation = false },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("SOS Alert Sent") },
                text = { Text("Your SOS alert has been created and saved. You can view it in Alert History.") },
                confirmButton = {
                    TextButton(onClick = { showSOSConfirmation = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // explains why we need location permissions
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                title = { Text("Permissions Required") },
                text = { Text("SafeStride needs location permissions to track your position during Safe Walk and detect if you might need help. Please grant these permissions to use this feature.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionRationale = false
                            requestPermissions()
                        }
                    ) {
                        Text("Grant Permissions")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // the "are you okay?" dialog when fall is detected
        if (showFallDetectionDialog) {
            FallDetectionDialog(
                onDismiss = {
                    Log.d("DashboardScreen", "Fall dialog dismissed")
                    showFallDetectionDialog = false
                    safeWalkViewModel.resetFallDetection()
                },
                onImOkay = {
                    Log.d("DashboardScreen", "User pressed I'm OK on Dashboard")
                    safeWalkViewModel.resetFallDetection()
                },
                onSendSOS = {
                    Log.w("DashboardScreen", "AUTOMATIC SOS from fall detection at lat=${currentLocation?.latitude}, lng=${currentLocation?.longitude}")
                    alertViewModel.createAutomaticAlert(
                        latitude = currentLocation?.latitude ?: 0.0,
                        longitude = currentLocation?.longitude ?: 0.0,
                        address = "Automatic alert - Fall detected"
                    )
                }
            )
        }
    }
}
