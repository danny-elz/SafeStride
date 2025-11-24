package week11.stn2002.safestride.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import week11.stn2002.safestride.navigation.Screen
import week11.stn2002.safestride.ui.components.CustomButton
import week11.stn2002.safestride.ui.components.CustomTextField
import week11.stn2002.safestride.ui.viewmodel.AuthViewModel
import week11.stn2002.safestride.util.Resource

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var emergencyContactError by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
                authViewModel.resetRegisterState()
            }
            is Resource.Error -> {
                // Error is shown in UI
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join SafeStride Today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = "Email",
            isError = emailError != null,
            errorMessage = emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = emergencyContact,
            onValueChange = {
                emergencyContact = it
                emergencyContactError = null
            },
            label = "Emergency Contact Phone",
            isError = emergencyContactError != null,
            errorMessage = emergencyContactError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Emergency Contact")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = "Password",
            isError = passwordError != null,
            errorMessage = passwordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null
            },
            label = "Confirm Password",
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (registerState is Resource.Error) {
            Text(
                text = (registerState as Resource.Error).message ?: "Registration failed",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        CustomButton(
            text = "Sign Up",
            onClick = {
                emailError = if (email.isBlank()) "Email is required" else null
                emergencyContactError = if (emergencyContact.isBlank()) "Emergency contact is required" else null
                passwordError = when {
                    password.isBlank() -> "Password is required"
                    password.length < 6 -> "Password must be at least 6 characters"
                    else -> null
                }
                confirmPasswordError = when {
                    confirmPassword.isBlank() -> "Please confirm password"
                    password != confirmPassword -> "Passwords do not match"
                    else -> null
                }

                if (emailError == null && passwordError == null &&
                    confirmPasswordError == null && emergencyContactError == null) {
                    authViewModel.register(email, password, emergencyContact)
                }
            },
            isLoading = registerState is Resource.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
