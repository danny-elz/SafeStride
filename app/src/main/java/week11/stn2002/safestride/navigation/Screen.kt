package week11.stn2002.safestride.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object AlertHistory : Screen("alert_history")
    object Profile : Screen("profile")
}
