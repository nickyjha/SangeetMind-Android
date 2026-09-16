package com.sangeetmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.sangeetmind.app.navigation.SangeetMindNavHost
import com.sangeetmind.core.database.PreferencesRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
