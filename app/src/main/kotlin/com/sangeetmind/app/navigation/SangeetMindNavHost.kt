package com.sangeetmind.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.sangeetmind.features.astrology.numerology.ui.NumerologyGlossaryScreen
import com.sangeetmind.features.astrology.numerology.ui.NumerologyScreen
import com.sangeetmind.features.astrology.panchang.ui.PanchangScreen
import com.sangeetmind.features.astrology.payments.ui.PaymentsScreen
import com.sangeetmind.features.astrology.readings.ui.ReadingsScreen
import com.sangeetmind.features.astrology.referrals.ui.ReferralScreen
import com.sangeetmind.features.astrology.reports.ui.ReportsScreen
import com.sangeetmind.features.astrology.sangeet.ui.SangeetScreen
import com.sangeetmind.features.astrology.holistic.ui.HolisticScreen
import com.sangeetmind.features.astrology.eclipse.ui.EclipseScreen
import com.sangeetmind.features.astrology.home.CalendarHubScreen
import com.sangeetmind.features.astrology.home.KundliHubScreen
import com.sangeetmind.features.astrology.home.MainBottomBar
import com.sangeetmind.features.astrology.home.MainTab
import com.sangeetmind.features.astrology.home.MeHubScreen
import com.sangeetmind.features.astrology.festival.ui.FestivalScreen
import com.sangeetmind.features.astrology.prashna.ui.PrashnaScreen
import com.sangeetmind.features.astrology.gochar.ui.GocharScreen
import com.sangeetmind.features.astrology.varshaphal.ui.VarshaphalScreen
import com.sangeetmind.features.auth.ui.AuthScreen
import com.sangeetmind.features.meditation.ui.MeditationScreen
import com.sangeetmind.features.onboarding.ui.OnboardingScreen
import com.sangeetmind.features.player.ui.PlayerScreen
import com.sangeetmind.features.raaglibrary.ui.RaagListScreen
import com.sangeetmind.features.settings.ui.SettingsScreen

/**
 * Main navigation host for the app.
 *
 * Post-auth the app is a 5-tab shell: Home ("dashboard") · Kundli · Ask (centre, pushes
 * ChatMind) · Calendar · Me. The bottom bar shows only on the four tab-root routes
 * ([MainTab.barRoutes]); every inner screen keeps its own top bar with a back arrow.
 * Tabs are siblings above Home in the back stack (popUpTo "dashboard"), so back from any
 * tab root lands on Home and back from Home exits the app.
 */
@Composable
fun SangeetMindNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding",
    // From a push notification tap: dashboard | readings | festivals | eclipses.
    deepLinkScreen: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    LaunchedEffect(deepLinkScreen) {
        // Only once signed in: the graph starts at onboarding/auth otherwise.
        if (startDestination == "dashboard") {
            when (deepLinkScreen) {
                "readings", "festivals", "eclipses" -> runCatching { navController.navigate(deepLinkScreen) }
                // "dashboard" now means the Home tab: drop whatever is above it.
                "dashboard" -> runCatching { navController.popBackStack(MainTab.HOME.route, inclusive = false) }
                else -> Unit
            }
        }
        if (deepLinkScreen != null) onDeepLinkConsumed()
    }
    // Re-navigating to "dashboard" with popUpTo(inclusive=true) recreates a fresh
    // Dashboard (and its Hilt ViewModel), so it always reflects the latest primary
    // kundli after returning from onboarding/list/etc.
    fun NavHostController.returnToFreshDashboard() {
        navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
    }

    fun selectTab(tab: MainTab) {
        when (tab) {
            MainTab.ASK -> navController.navigate("chatmind")
            MainTab.HOME -> navController.popBackStack(MainTab.HOME.route, inclusive = false)
            else -> navController.navigate(tab.route) {
                // Keep Home at the bottom; tab roots replace each other above it.
                popUpTo(MainTab.HOME.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in MainTab.barRoutes

    Scaffold(
        // Inner screens handle their own system-bar insets; the shell only adds the bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(current = MainTab.forRoute(currentRoute), onSelect = ::selectTab)
            }
        }
    ) { shellPadding ->
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize().padding(shellPadding)
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

        // Home tab (route kept as "dashboard": post-auth start + push deep links).
        composable(MainTab.HOME.route) {
            DashboardScreen(
                onOpenKundliList = { navController.navigate("kundli_list") },
                onOpenKundliOnboarding = { navController.navigate("kundli_onboarding") },
                onOpenHoroscope = { navController.navigate("horoscope") },
                onOpenChart = { navController.navigate("chart") },
                onOpenReadings = { navController.navigate("readings") },
                onOpenMatch = { navController.navigate("match") },
                onOpenPanchang = { navController.navigate("panchang") },
                onOpenChatMind = { navController.navigate("chatmind") },
                onAskChatMind = { q -> navController.navigate("chatmind?q=${Uri.encode(q)}") }
            )
        }

        // Kundli tab: chart-derived features + all readings + match + PDF reports.
        composable(MainTab.KUNDLI.route) {
            KundliHubScreen(
                onOpenChart = { navController.navigate("chart") },
                onOpenGochar = { navController.navigate("gochar") },
                onOpenVarshaphal = { navController.navigate("varshaphal") },
                onOpenNumerology = { navController.navigate("numerology") },
                onOpenReadings = { navController.navigate("readings") },
                onOpenInterpretation = { navController.navigate("interpretation") },
                onOpenHolistic = { navController.navigate("holistic") },
                onOpenMatch = { navController.navigate("match") },
                onOpenReports = { navController.navigate("reports") }
            )
        }

        // Calendar tab: horoscope, panchang, festivals, eclipses, muhurat, prashna.
        composable(MainTab.CALENDAR.route) {
            CalendarHubScreen(
                onOpenHoroscope = { navController.navigate("horoscope") },
                onOpenPanchang = { navController.navigate("panchang") },
                onOpenFestivals = { navController.navigate("festivals") },
                onOpenEclipses = { navController.navigate("eclipses") },
                onOpenMuhurat = { navController.navigate("muhurat") },
                onOpenPrashna = { navController.navigate("prashna") }
            )
        }

        // Me tab: kundli switcher, Premium & Wallet, referrals, language, settings, legal.
        composable(MainTab.ME.route) {
            MeHubScreen(
                onOpenKundliList = { navController.navigate("kundli_list") },
                onOpenPayments = { navController.navigate("payments") },
                onOpenReferrals = { navController.navigate("referrals") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenSangeet = { navController.navigate("sangeet") }
            )
        }

        // Kundli onboarding (first birth-details entry, or adding another kundli)
        composable("kundli_onboarding") {
            KundliOnboardingScreen(
                onSaved = { navController.returnToFreshDashboard() },
                onNavigateBack = { navController.popFrom(it) }
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
            HoroscopeScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("panchang") {
            PanchangScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("muhurat") {
            MuhuratScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("match") {
            MatchScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("numerology") {
            NumerologyScreen(
                onNavigateBack = { navController.popFrom(it) },
                onOpenGlossary = { navController.navigate("numerology_glossary") }
            )
        }

        composable("numerology_glossary") {
            NumerologyGlossaryScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("interpretation") {
            InterpretationScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("chart") {
            ChartScreen(
                onNavigateBack = { navController.popFrom(it) },
                onOpenDashaStory = { navController.navigate("readings?tab=DASHA") }
            )
        }

        composable(
            "chatmind?q={q}",
            arguments = listOf(navArgument("q") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ChatMindScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("payments") {
            PaymentsScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("reports") {
            ReportsScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable(
            "readings?tab={tab}",
            arguments = listOf(navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ReadingsScreen(onNavigateBack = { navController.popFrom(it) })
        }

        // Marketplace: no entry point any more (tile removed); routes kept for later.
        composable("marketplace") {
            MarketplaceListScreen(
                onNavigateBack = { navController.popFrom(it) },
                onOpenAstrologer = { id -> navController.navigate("marketplace/$id") }
            )
        }

        composable(
            route = "marketplace/{astrologerId}",
            arguments = listOf(navArgument("astrologerId") { type = NavType.StringType })
        ) {
            MarketplaceDetailScreen(
                onNavigateBack = { navController.popFrom(it) },
                onStartChat = { id -> navController.navigate("marketplace/$id/chat") }
            )
        }

        composable(
            route = "marketplace/{astrologerId}/chat",
            arguments = listOf(navArgument("astrologerId") { type = NavType.StringType })
        ) {
            MarketplaceChatScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("referrals") {
            ReferralScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("sangeet") {
            SangeetScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("gochar") {
            GocharScreen(onNavigateBack = { navController.popFrom(it) })
        }
        composable("prashna") {
            PrashnaScreen(onNavigateBack = { navController.popFrom(it) })
        }
        composable("festivals") {
            FestivalScreen(onNavigateBack = { navController.popFrom(it) })
        }
        composable("eclipses") {
            EclipseScreen(onNavigateBack = { navController.popFrom(it) })
        }
        composable("varshaphal") {
            VarshaphalScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("holistic") {
            HolisticScreen(onNavigateBack = { navController.popFrom(it) })
        }

        // Legacy raag/player/meditation screens — kept for a later phase, not reachable
        // from any UI. (The "sangeet" route above is the remedy-music screen, listed in Me.)
        composable("raag_library") {
            RaagListScreen(
                onRaagClick = { navController.navigate("player") }
            )
        }

        composable("player") {
            PlayerScreen(onNavigateBack = { navController.popFrom(it) })
        }

        composable("meditation") {
            MeditationScreen()
        }

        composable("settings") {
            SettingsScreen(
                onAccountDeleted = { navController.navigate("auth") { popUpTo(0) { inclusive = true } } },
                onNavigateBack = { navController.popFrom(it) }
            )
        }
    }
    }
}

/**
 * Back from an inner screen: pop only if that screen is still the resumed one. A fast
 * double-tap on a top-bar back arrow (or back arrow + system back) otherwise pops twice
 * and can skip the tab root / exit the app.
 */
private fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) popBackStack()
}
