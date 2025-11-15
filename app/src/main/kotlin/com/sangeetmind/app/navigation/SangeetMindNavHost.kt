package com.sangeetmind.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sangeetmind.features.astrology.ui.AstrologyScreen
import com.sangeetmind.features.auth.ui.AuthScreen
import com.sangeetmind.features.meditation.ui.MeditationScreen
import com.sangeetmind.features.onboarding.ui.OnboardingScreen
import com.sangeetmind.features.player.ui.PlayerScreen
import com.sangeetmind.features.raaglibrary.ui.RaagListScreen
import com.sangeetmind.features.settings.ui.SettingsScreen

/**
 * Main navigation host for the app
 */
@Composable
fun SangeetMindNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding" // Change to "raag_library" after onboarding
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    navController.navigate("auth") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // Auth
        composable("auth") {
            AuthScreen(
                isSignup = false,
                onAuthSuccess = {
                    navController.navigate("raag_library") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onToggleMode = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            AuthScreen(
                isSignup = true,
                onAuthSuccess = {
                    navController.navigate("raag_library") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onToggleMode = {
                    navController.popBackStack()
                }
            )
        }

        // Raag Library
        composable("raag_library") {
            RaagListScreen(
                onRaagClick = { raagId ->
                    // TODO: Navigate to raag detail screen
                    navController.navigate("player")
                }
            )
        }

        // Player
        composable("player") {
            PlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Meditation
        composable("meditation") {
            MeditationScreen()
        }

        // Astrology
        composable("astrology") {
            AstrologyScreen()
        }

        // Settings
        composable("settings") {
            SettingsScreen()
        }
    }
}

