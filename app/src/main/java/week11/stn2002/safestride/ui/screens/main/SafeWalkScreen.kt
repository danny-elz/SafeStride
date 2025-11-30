package week11.stn2002.safestride.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import week11.stn2002.safestride.ui.theme.AlertRed
import week11.stn2002.safestride.ui.theme.SafeBlue40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeWalkScreen(
    navController: NavController,
    elapsedTime: Long,
    onStopSafeWalk: () -> Unit,
    onEmergencySOS: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeStride") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafeBlue40,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                }
            )
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
                text = "Safe Walk Active",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Monitoring card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD) // Light blue
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📍",
                        fontSize = 56.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Monitoring your location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = SafeBlue40
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer display
                    Text(
                        text = formatElapsedTime(elapsedTime),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeBlue40
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stop Safe Walk button
            Button(
                onClick = onStopSafeWalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRed
                )
            ) {
                Text(
                    text = "Stop Safe Walk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency SOS button
            Button(
                onClick = onEmergencySOS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRed
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY SOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Info text
            Text(
                text = "Fall detection is active. If a fall is detected, you will be asked if you're okay.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
