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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.razorpay.Checkout
import com.sangeetmind.features.astrology.payments.PaymentsTab
import com.sangeetmind.features.astrology.payments.PaymentsViewModel
import com.sangeetmind.libs.models.RazorpayOrder
import com.sangeetmind.libs.models.Sku
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingOrder) {
        val order = uiState.pendingOrder ?: return@LaunchedEffect
        val activity = context as? Activity
        if (activity != null) {
            launchRazorpayCheckout(activity, order)
        }
        viewModel.clearPendingOrder()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium & Wallet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    text = { Text("Premium") }
                )
                Tab(
                    selected = uiState.tab == PaymentsTab.WALLET,
                    onClick = { viewModel.setTab(PaymentsTab.WALLET) },
                    text = { Text("Wallet") }
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

            when (uiState.tab) {
                PaymentsTab.PREMIUM -> PremiumTab(
                    isPremium = uiState.premiumStatus?.premium == true,
                    plans = uiState.premiumPlans,
                    onBuy = viewModel::buyPremium
                )
                PaymentsTab.WALLET -> WalletTab(
                    balancePaise = uiState.walletBalancePaise,
                    presets = uiState.walletPresets,
                    transactions = uiState.walletTransactions,
                    onRecharge = viewModel::rechargeWallet
                )
            }
        }
    }
}

@Composable
private fun PremiumTab(isPremium: Boolean, plans: List<Sku>, onBuy: (Sku) -> Unit) {
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
                        Text("You're a Premium member")
                    }
                }
            }
        }
        items(plans) { sku -> SkuCard(sku, buttonLabel = "Subscribe", onClick = { onBuy(sku) }) }
    }
}

@Composable
private fun WalletTab(
    balancePaise: Long?,
    presets: List<Sku>,
    transactions: List<com.sangeetmind.libs.models.WalletTransaction>,
    onRecharge: (Sku) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Balance", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatPaise(balancePaise ?: 0),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
        item { Text("Top up", style = MaterialTheme.typography.titleMedium) }
        items(presets) { sku -> SkuCard(sku, buttonLabel = "Add", onClick = { onRecharge(sku) }) }
        if (transactions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recent recharges", style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun SkuCard(sku: Sku, buttonLabel: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sku.label, style = MaterialTheme.typography.titleMedium)
                Text(formatPaise(sku.pricePaise), style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onClick) { Text(buttonLabel) }
        }
    }
}

private fun formatPaise(paise: Long): String {
    val rupees = paise / 100.0
    return "₹%.2f".format(rupees)
}

private fun launchRazorpayCheckout(activity: Activity, order: RazorpayOrder) {
    val checkout = Checkout()
    checkout.setKeyID(order.keyId)
    val options = JSONObject().apply {
        put("name", "SangeetMind")
        put("order_id", order.orderId)
        put("currency", order.currency)
        put("amount", order.amount.toString())
    }
    checkout.open(activity, options)
}
