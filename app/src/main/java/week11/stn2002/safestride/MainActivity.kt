package week11.stn2002.safestride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import week11.stn2002.safestride.navigation.NavGraph
import week11.stn2002.safestride.navigation.Screen
import week11.stn2002.safestride.ui.theme.SafeStrideTheme
import week11.stn2002.safestride.ui.viewmodel.AlertViewModel
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeStrideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val alertViewModel: AlertViewModel = viewModel()

                    val startDestination = if (authViewModel.isUserLoggedIn) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        alertViewModel = alertViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}