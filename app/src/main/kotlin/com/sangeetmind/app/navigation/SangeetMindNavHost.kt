package com.sangeetmind.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sangeetmind.features.astrology.chatmind.ui.ChatMindScreen
import com.sangeetmind.features.astrology.chart.ui.ChartScreen
import com.sangeetmind.features.astrology.dashboard.ui.DashboardScreen
import com.sangeetmind.features.astrology.horoscope.ui.HoroscopeScreen
import com.sangeetmind.features.astrology.interpretation.ui.InterpretationScreen
import com.sangeetmind.features.astrology.kundli.ui.KundliListScreen
import com.sangeetmind.features.astrology.kundli.ui.KundliOnboardingScreen
import com.sangeetmind.features.astrology.marketplace.ui.MarketplaceChatScreen
import com.sangeetmind.features.astrology.marketplace.ui.MarketplaceDetailScreen
import com.sangeetmind.features.astrology.marketplace.ui.MarketplaceListScreen
import com.sangeetmind.features.astrology.match.ui.MatchScreen
import com.sangeetmind.features.astrology.muhurat.ui.MuhuratScreen
import com.sangeetmind.features.astrology.numerology.ui.NumerologyScreen
import com.sangeetmind.features.astrology.panchang.ui.PanchangScreen
import com.sangeetmind.features.astrology.payments.ui.PaymentsScreen
import com.sangeetmind.features.astrology.readings.ui.ReadingsScreen
import com.sangeetmind.features.astrology.referrals.ui.ReferralScreen
import com.sangeetmind.features.astrology.reports.ui.ReportsScreen
import com.sangeetmind.features.astrology.sangeet.ui.SangeetScreen
import com.sangeetmind.features.astrology.varshaphal.ui.VarshaphalScreen
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
                onOpenInterpretation = { navController.navigate("interpretation") },
                onOpenChart = { navController.navigate("chart") },
                onOpenChatMind = { navController.navigate("chatmind") },
                onOpenPayments = { navController.navigate("payments") },
                onOpenReports = { navController.navigate("reports") },
                onOpenReadings = { navController.navigate("readings") },
                onOpenMarketplace = { navController.navigate("marketplace") },
                onOpenReferrals = { navController.navigate("referrals") },
                onOpenSangeet = { navController.navigate("sangeet") },
                onOpenVarshaphal = { navController.navigate("varshaphal") }
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

        composable("chart") {
            ChartScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("chatmind") {
            ChatMindScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("payments") {
            PaymentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("reports") {
            ReportsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("readings") {
            ReadingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("marketplace") {
            MarketplaceListScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenAstrologer = { id -> navController.navigate("marketplace/$id") }
            )
        }

        composable(
            route = "marketplace/{astrologerId}",
            arguments = listOf(navArgument("astrologerId") { type = NavType.StringType })
        ) {
            MarketplaceDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartChat = { id -> navController.navigate("marketplace/$id/chat") }
            )
        }

        composable(
            route = "marketplace/{astrologerId}/chat",
            arguments = listOf(navArgument("astrologerId") { type = NavType.StringType })
        ) {
            MarketplaceChatScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("referrals") {
            ReferralScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("sangeet") {
            SangeetScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("varshaphal") {
            VarshaphalScreen(onNavigateBack = { navController.popBackStack() })
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
