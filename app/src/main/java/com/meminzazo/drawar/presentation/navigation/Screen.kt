package com.meminzazo.drawar.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meminzazo.drawar.presentation.screen.home.HomeScreen
import com.meminzazo.drawar.presentation.screen.simple.SimpleScreen
import com.meminzazo.drawar.presentation.screen.pro.ProScreen

sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Simple : Screen("simple")
    object Pro    : Screen("pro")
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
                },
                onProModeClick = {
                    navController.navigate(Screen.Pro.route)
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
        composable(Screen.Pro.route) {
            ProScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
