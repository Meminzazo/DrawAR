package com.meminzazo.drawar.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meminzazo.drawar.presentation.screen.home.HomeScreen
import com.meminzazo.drawar.presentation.screen.simple.SimpleScreen

sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Simple : Screen("simple")
}

@Composable
fun DrawARNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSimpleModeClick = {
                    navController.navigate(Screen.Simple.route)
                }
            )
        }
        composable(Screen.Simple.route) {
            SimpleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}