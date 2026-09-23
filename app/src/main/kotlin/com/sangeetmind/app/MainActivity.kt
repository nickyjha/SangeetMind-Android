package com.sangeetmind.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.sangeetmind.app.navigation.SangeetMindNavHost
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.database.PreferencesRepository
import com.sangeetmind.core.ui.language.LocalAppLanguage
import com.sangeetmind.core.ui.language.LocalLanguageSwitcher
import com.sangeetmind.core.ui.theme.SangeetMindTheme
import com.sangeetmind.features.astrology.payments.RazorpayResult
import com.sangeetmind.features.astrology.payments.RazorpayResultBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    @Inject
    lateinit var razorpayResultBus: RazorpayResultBus

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var languageManager: LanguageManager

    // The activity's own resources start in the saved language too, so the first frame
    // never flashes English. Read synchronously — Hilt hasn't injected anything yet here.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLanguage(LanguageManager.readSync(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Language layer: every stringResource() below reads LocalContext/LocalConfiguration,
            // so swapping in a locale-overridden context re-resolves the whole tree in place —
            // no activity recreate, navigation state preserved.
            val language by languageManager.language.collectAsStateWithLifecycle()
            val activityContext = LocalContext.current
            val localizedContext = remember(language) { activityContext.withAppLanguage(language) }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalAppLanguage provides language,
                LocalLanguageSwitcher provides languageManager::setLanguage
            ) {
                SangeetMindTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var startDestination by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(Unit) {
                            startDestination = when {
                                FirebaseAuth.getInstance().currentUser != null -> "dashboard"
                                preferencesRepository.isOnboardingCompleted() -> "auth"
                                else -> "onboarding"
                            }
                        }

                        val destination = startDestination
                        if (destination == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            SangeetMindNavHost(startDestination = destination)
                        }
                    }
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val orderId = paymentData?.orderId
        val signature = paymentData?.signature
        if (razorpayPaymentId != null && orderId != null && signature != null) {
            razorpayResultBus.emit(RazorpayResult.Success(orderId, razorpayPaymentId, signature))
        } else {
            razorpayResultBus.emit(RazorpayResult.Failure(-1, "Incomplete payment data"))
        }
    }

    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        razorpayResultBus.emit(RazorpayResult.Failure(code, description))
    }
}
