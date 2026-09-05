package com.sangeetmind.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sangeetmind.features.astrology.dashboard.ui.DashboardScreen
import com.sangeetmind.features.astrology.horoscope.ui.HoroscopeScreen
import com.sangeetmind.features.astrology.interpretation.ui.InterpretationScreen
import com.sangeetmind.features.astrology.kundli.ui.KundliListScreen
import com.sangeetmind.features.astrology.kundli.ui.KundliOnboardingScreen
import com.sangeetmind.features.astrology.match.ui.MatchScreen
import com.sangeetmind.features.astrology.muhurat.ui.MuhuratScreen
import com.sangeetmind.features.astrology.numerology.ui.NumerologyScreen
import com.sangeetmind.features.astrology.panchang.ui.PanchangScreen
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
    startDestination: String = "onboarding"
) {
    // Re-navigating to "dashboard" with popUpTo(inclusive=true) recreates a fresh
    // Dashboard (and its Hilt ViewModel), so it always reflects the latest primary
    // kundli after returning from onboarding/list/etc.
    fun NavHostController.returnToFreshDashboard() {
        navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
    }

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
                    navController.navigate("dashboard") {
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
                    navController.navigate("dashboard") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onToggleMode = {
                    navController.popBackStack()
                }
            )
        }

        // Dashboard — post-auth landing screen
        composable("dashboard") {
            DashboardScreen(
                onOpenKundliList = { navController.navigate("kundli_list") },
                onOpenKundliOnboarding = { navController.navigate("kundli_onboarding") },
                onOpenHoroscope = { navController.navigate("horoscope") },
                onOpenPanchang = { navController.navigate("panchang") },
                onOpenMuhurat = { navController.navigate("muhurat") },
                onOpenMatch = { navController.navigate("match") },
                onOpenNumerology = { navController.navigate("numerology") },
                onOpenInterpretation = { navController.navigate("interpretation") }
            )
        }

        // Kundli onboarding (first birth-details entry, or adding another kundli)
        composable("kundli_onboarding") {
            KundliOnboardingScreen(
                onSaved = { navController.returnToFreshDashboard() }
            )
        }

        // Kundli list / switcher
        composable("kundli_list") {
            KundliListScreen(
                onNavigateBack = { navController.returnToFreshDashboard() },
                onAddKundli = { navController.navigate("kundli_onboarding") }
            )
        }

        composable("horoscope") {
            HoroscopeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("panchang") {
            PanchangScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("muhurat") {
            MuhuratScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("match") {
            MatchScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("numerology") {
            NumerologyScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("interpretation") {
            InterpretationScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Sangeet (raag/meditation) — kept for a later phase, reachable but not
        // part of the default post-auth flow yet.
        composable("raag_library") {
            RaagListScreen(
                onRaagClick = { navController.navigate("player") }
            )
        }

        composable("player") {
            PlayerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("meditation") {
            MeditationScreen()
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
