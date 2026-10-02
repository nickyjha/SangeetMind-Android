package com.sangeetmind.features.astrology.payments.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.razorpay.Checkout
import com.sangeetmind.core.common.language.findActivity
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.features.astrology.R
import com.sangeetmind.features.astrology.payments.PaymentsTab
import com.sangeetmind.features.astrology.payments.PaymentsViewModel
import com.sangeetmind.features.astrology.payments.PlayProducts
import com.sangeetmind.features.astrology.payments.PremiumPlan
import com.sangeetmind.features.astrology.payments.WalletPack
import com.sangeetmind.libs.models.RazorpayOrder
import org.json.JSONObject

/**
 * Premium + Wallet. Inside the Android app both are bought through Google Play Billing
 * (policy); prices come from the Play product details. The Razorpay checkout hook below is
 * kept for the website-style flow but nothing on this screen triggers it any more.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // LocalContext is the language-layer wrapper, not the Activity itself; unwrap it.
    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(uiState.pendingOrder) {
        val order = uiState.pendingOrder ?: return@LaunchedEffect
        if (activity != null) {
            launchRazorpayCheckout(activity, order)
        }
        viewModel.clearPendingOrder()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payments_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CoreR.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == PaymentsTab.PREMIUM,
                    onClick = { viewModel.setTab(PaymentsTab.PREMIUM) },
                    text = { Text(stringResource(R.string.payments_tab_premium)) }
                )
                Tab(
                    selected = uiState.tab == PaymentsTab.WALLET,
                    onClick = { viewModel.setTab(PaymentsTab.WALLET) },
                    text = { Text(stringResource(R.string.payments_tab_wallet)) }
                )
            }

            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(uiState.error!!, modifier = Modifier.padding(12.dp))
                }
            }
            if (uiState.message != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(uiState.message!!, modifier = Modifier.padding(12.dp))
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            val canBuy = activity != null && uiState.billingAvailable != false
            when (uiState.tab) {
                PaymentsTab.PREMIUM -> PremiumTab(
                    isPremium = uiState.premiumStatus?.premium == true,
                    premiumEndsAt = uiState.premiumStatus?.endsAt,
                    plans = PlayProducts.premiumPlans,
                    playPrices = uiState.playPrices,
                    canBuy = canBuy,
                    onBuy = { plan -> activity?.let { viewModel.buyPremiumPlan(it, plan.productId) } }
                )
                PaymentsTab.WALLET -> WalletTab(
                    balancePaise = uiState.walletBalancePaise,
                    packs = PlayProducts.walletPacks,
                    playPrices = uiState.playPrices,
                    canBuy = canBuy,
                    transactions = uiState.walletTransactions,
                    onRecharge = { pack -> activity?.let { viewModel.buyWalletPack(it, pack.productId) } }
                )
            }
        }
    }
}

@Composable
private fun PremiumTab(
    isPremium: Boolean,
    premiumEndsAt: String?,
    plans: List<PremiumPlan>,
    playPrices: Map<String, String>,
    canBuy: Boolean,
    onBuy: (PremiumPlan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (isPremium) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.payments_premium_member))
                            val until = premiumEndsAt?.take(10)
                            if (!until.isNullOrBlank()) {
                                Text(
                                    stringResource(R.string.payments_premium_until, until),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
        items(plans) { plan ->
            val label = when (plan.basePlanId) {
                "yearly" -> stringResource(R.string.payments_premium_yearly)
                else -> stringResource(R.string.payments_premium_monthly)
            }
            val period = when (plan.basePlanId) {
                "yearly" -> stringResource(R.string.payments_per_year)
                else -> stringResource(R.string.payments_per_month)
            }
            PlayProductCard(
                title = label,
                price = playPrices[plan.productId]?.let { "$it $period" }
                    ?: stringResource(R.string.payments_price_loading),
                buttonLabel = stringResource(R.string.payments_subscribe),
                enabled = canBuy,
                onClick = { onBuy(plan) }
            )
        }
        item {
            Text(
                stringResource(R.string.payments_play_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WalletTab(
    balancePaise: Long?,
    packs: List<WalletPack>,
    playPrices: Map<String, String>,
    canBuy: Boolean,
    transactions: List<com.sangeetmind.libs.models.WalletTransaction>,
    onRecharge: (WalletPack) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.payments_balance), style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatPaise(balancePaise ?: 0),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
        item { Text(stringResource(R.string.payments_top_up), style = MaterialTheme.typography.titleMedium) }
        items(packs) { pack ->
            PlayProductCard(
                title = stringResource(R.string.payments_wallet_pack, formatPaise(pack.paise)),
                price = playPrices[pack.productId] ?: stringResource(R.string.payments_price_loading),
                buttonLabel = stringResource(R.string.payments_add),
                enabled = canBuy,
                onClick = { onRecharge(pack) }
            )
        }
        if (transactions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.payments_recent_recharges), style = MaterialTheme.typography.titleMedium)
            }
            items(transactions) { tx ->
                ListItem(
                    headlineContent = { Text(formatPaise(tx.amountPaise)) },
                    supportingContent = { Text(tx.createdAt ?: "") }
                )
            }
        }
    }
}

/** Same card shape as the old SKU card: title + price on the left, action on the right. */
@Composable
private fun PlayProductCard(
    title: String,
    price: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(price, style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onClick, enabled = enabled) { Text(buttonLabel) }
        }
    }
}

private fun formatPaise(paise: Long): String {
    val rupees = paise / 100.0
    return if (paise % 100 == 0L) "₹${paise / 100}" else "₹%.2f".format(rupees)
}

private fun launchRazorpayCheckout(activity: Activity, order: RazorpayOrder) {
    val checkout = Checkout()
    checkout.setKeyID(order.keyId)
    val options = JSONObject().apply {
        put("name", "AstroGeet")
        put("order_id", order.orderId)
        put("currency", order.currency)
        put("amount", order.amount.toString())
    }
    checkout.open(activity, options)
}
