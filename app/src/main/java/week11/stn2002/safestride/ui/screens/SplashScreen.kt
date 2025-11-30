package week11.stn2002.safestride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import week11.stn2002.safestride.ui.theme.SafeBlue40
import week11.stn2002.safestride.ui.theme.SafeGreen40

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SafeBlue40, SafeGreen40)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo icon container
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(30.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚶",
                        fontSize = 60.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // App name
            Text(
                text = "SafeStride",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                text = "Your Personal Safety Companion",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Get Started button
            Button(
                onClick = onTimeout,
                modifier = Modifier
                    .padding(horizontal = 48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SafeBlue40
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}
