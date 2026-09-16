package com.sangeetmind.features.astrology.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetmind.core.common.Result
import com.sangeetmind.libs.models.PremiumStatus
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.Sku
import com.sangeetmind.libs.models.WalletTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class PaymentsTab { PREMIUM, WALLET }

data class PaymentsUiState(
    val tab: PaymentsTab = PaymentsTab.PREMIUM,
    val isLoading: Boolean = false,
    val premiumPlans: List<Sku> = emptyList(),
    val walletPresets: List<Sku> = emptyList(),
    val premiumStatus: PremiumStatus? = null,
    val walletBalancePaise: Long? = null,
    val walletTransactions: List<WalletTransaction> = emptyList(),
    val pendingOrder: RazorpayOrder? = null,
    val pendingIsWalletRecharge: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repository: PaymentsRepository,
    private val resultBus: RazorpayResultBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            resultBus.results.collect { result ->
                when (result) {
                    is RazorpayResult.Success -> onCheckoutSuccess(result)
                    is RazorpayResult.Failure -> _uiState.update {
                        it.copy(pendingOrder = null, error = result.description ?: "Payment failed")
                    }
                }
            }
        }
    }

    fun setTab(tab: PaymentsTab) {
        _uiState.update { it.copy(tab = tab, error = null, message = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val premiumResult = repository.getSkus("subscription")
            val walletSkusResult = repository.getSkus("recharge")
            val statusResult = repository.getPremiumStatus()
            val balanceResult = repository.getWalletBalance()
            val txResult = repository.getWalletTransactions()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    premiumPlans = (premiumResult as? Result.Success)?.data ?: state.premiumPlans,
                    walletPresets = (walletSkusResult as? Result.Success)?.data ?: state.walletPresets,
                    premiumStatus = (statusResult as? Result.Success)?.data ?: state.premiumStatus,
                    walletBalancePaise = (balanceResult as? Result.Success)?.data?.balancePaise
                        ?: state.walletBalancePaise,
                    walletTransactions = (txResult as? Result.Success)?.data ?: state.walletTransactions
                )
            }
        }
    }

    fun buyPremium(sku: Sku) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createPremiumOrder(sku.skuId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, pendingOrder = result.data, pendingIsWalletRecharge = false)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun rechargeWallet(sku: Sku) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createWalletOrder(sku.skuId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, pendingOrder = result.data, pendingIsWalletRecharge = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Consumed by the screen right after it calls Checkout.open() for [PaymentsUiState.pendingOrder]. */
    fun clearPendingOrder() {
        _uiState.update { it.copy(pendingOrder = null) }
    }

    private fun onCheckoutSuccess(success: RazorpayResult.Success) {
        val wasWalletRecharge = _uiState.value.pendingIsWalletRecharge
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pendingOrder = null) }
            if (wasWalletRecharge) {
                // Wallet recharges settle via the Razorpay webhook alone (no verify-payment
                // route for wallet orders) — give it a moment, then refresh the balance.
                delay(2000)
                refresh()
                _uiState.update { it.copy(isLoading = false, message = "Payment received — updating balance") }
            } else {
                when (val result = repository.verifyPremiumPayment(
                    success.orderId, success.paymentId, success.signature
                )) {
                    is Result.Success -> {
                        refresh()
                        _uiState.update { it.copy(isLoading = false, message = "You're now Premium!") }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                    is Result.Loading -> Unit
                }
            }
        }
    }

    fun newIdempotencyKey(): String = UUID.randomUUID().toString()
}
