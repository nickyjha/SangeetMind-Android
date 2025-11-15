package com.sangeetmind.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sangeetmind.features.raaglibrary.ui.RaagListScreen

/**
 * Main navigation host for the app
 * TODO: Add navigation for all features
 */
@Composable
fun SangeetMindNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "raag_library"
    ) {
        composable("raag_library") {
            RaagListScreen(
                onRaagClick = { raagId ->
                    // TODO: Navigate to raag detail screen
                    navController.navigate("raag_detail/$raagId")
                }
            )
        }
        
        // TODO: Add more navigation destinations
        // composable("raag_detail/{raagId}") { ... }
        // composable("player") { ... }
        // composable("meditation") { ... }
        // composable("astrology") { ... }
        // composable("settings") { ... }
    }
}

