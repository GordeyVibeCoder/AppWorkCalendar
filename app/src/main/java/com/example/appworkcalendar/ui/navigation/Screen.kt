package com.example.appworkcalendar.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Дом")
    data object Earnings : Screen("earnings", "Заработок")
    data object Profile : Screen("profile", "Профиль")
}
