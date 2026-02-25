package com.example.appworkcalendar.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Earnings : Screen("earnings")
    data object Profile : Screen("profile")
}
