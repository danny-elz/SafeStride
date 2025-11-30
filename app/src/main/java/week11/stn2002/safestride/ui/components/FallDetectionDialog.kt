package week11.stn2002.safestride.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import week11.stn2002.safestride.ui.theme.AlertRed
import week11.stn2002.safestride.ui.theme.SafeGreen40

@Composable
fun FallDetectionDialog(
    onDismiss: () -> Unit,
    onImOkay: () -> Unit,
    onSendSOS: () -> Unit,
    countdownSeconds: Int = 30
) {
    var remainingSeconds by remember { mutableIntStateOf(countdownSeconds) }

    // countdown that auto-sends SOS if user doesn't respond in time
    LaunchedEffect(Unit) {
        Log.d("FallDetectionDialog", "Countdown started: $countdownSeconds seconds")
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            Log.d("FallDetectionDialog", "Countdown: $remainingSeconds seconds remaining")
        }
        // time's up - send the alert and close the dialog
        Log.w("FallDetectionDialog", "COUNTDOWN FINISHED - Auto-triggering SOS alert!")
        onSendSOS()
        onDismiss()
    }

    // makes the warning icon pulse to grab attention
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = { /* Don't allow dismiss by clicking outside */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing warning icon
                Text(
                    text = "⚠️",
                    fontSize = 80.sp,
                    modifier = Modifier.scale(scale)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Are you okay?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Fall detected! Please confirm you are safe",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Countdown display
                Text(
                    text = "Auto-alert in",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = AlertRed
                )

                Text(
                    text = "$remainingSeconds",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )

                Text(
                    text = "seconds",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = AlertRed
                )

                Spacer(modifier = Modifier.height(24.dp))

                // user taps this if they're fine and don't need help
                Button(
                    onClick = {
                        Log.d("FallDetectionDialog", "User pressed 'I'm OK' - cancelling SOS")
                        onImOkay()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeGreen40
                    )
                ) {
                    Text(
                        text = "I'm OK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // user taps this if they need immediate help
                Button(
                    onClick = {
                        Log.w("FallDetectionDialog", "User pressed 'Send SOS Now' - sending emergency alert!")
                        onSendSOS()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed
                    )
                ) {
                    Text(
                        text = "Send SOS Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
