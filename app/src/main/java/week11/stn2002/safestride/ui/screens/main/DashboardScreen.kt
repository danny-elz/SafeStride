package week11.stn2002.safestride.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import week11.stn2002.safestride.navigation.Screen
import week11.stn2002.safestride.ui.viewmodel.AlertViewModel
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    alertViewModel: AlertViewModel
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSafeWalkActive by remember { mutableStateOf(false) }
    var showSOSConfirmation by remember { mutableStateOf(false) }

    val createAlertState by alertViewModel.createAlertState.collectAsState()

    // Handle alert creation success
    LaunchedEffect(createAlertState) {
        if (createAlertState is week11.stn2002.safestride.util.Resource.Success) {
            showSOSConfirmation = true
            alertViewModel.resetCreateAlertState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeStride") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSafeWalkActive) {
                FloatingActionButton(
                    onClick = { /* TODO: Start Safe Walk Service */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = "Start Safe Walk")
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
                text = if (isSafeWalkActive) "Safe Walk Active" else "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isSafeWalkActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Monitoring your location",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isSafeWalkActive = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Stop Safe Walk")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    // Create manual SOS alert with placeholder coordinates
                    alertViewModel.createManualAlert(
                        latitude = 0.0,
                        longitude = 0.0,
                        address = "Manual SOS triggered from Dashboard"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "SOS",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SOS ALERT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { navController.navigate(Screen.AlertHistory.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = "History")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Alert History")
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
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
    }
}
