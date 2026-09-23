package com.sangeetmind.features.onboarding.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LanguagePickerAction
import com.sangeetmind.features.onboarding.OnboardingViewModel
import com.sangeetmind.features.onboarding.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val onboardingPages = listOf(
    OnboardingPage(
        title = R.string.onboarding_page1_title,
        description = R.string.onboarding_page1_desc,
        icon = Icons.Default.MusicNote
    ),
    OnboardingPage(
        title = R.string.onboarding_page2_title,
        description = R.string.onboarding_page2_desc,
        icon = Icons.Default.Star
    ),
    OnboardingPage(
        title = R.string.onboarding_page3_title,
        description = R.string.onboarding_page3_desc,
        icon = Icons.Default.CloudDownload
    )
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = { LanguagePickerAction() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.completeOnboarding(onComplete) }) {
                    Text(stringResource(CoreR.string.common_skip))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Consent checkboxes (last page only)
            if (pagerState.currentPage == onboardingPages.size - 1) {
                ConsentSection(
                    hasAcceptedTerms = uiState.hasAcceptedTerms,
                    hasAcceptedPrivacy = uiState.hasAcceptedPrivacy,
                    onAcceptTerms = viewModel::setAcceptedTerms,
                    onAcceptPrivacy = viewModel::setAcceptedPrivacy
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(CoreR.string.common_back))
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == onboardingPages.size - 1) {
                            viewModel.completeOnboarding(onComplete)
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = if (pagerState.currentPage == onboardingPages.size - 1) {
                        uiState.hasAcceptedTerms && uiState.hasAcceptedPrivacy
                    } else {
                        true
                    }
                ) {
                    Text(
                        stringResource(
                            if (pagerState.currentPage == onboardingPages.size - 1) {
                                R.string.onboarding_get_started
                            } else {
                                CoreR.string.common_next
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(page.description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConsentSection(
    hasAcceptedTerms: Boolean,
    hasAcceptedPrivacy: Boolean,
    onAcceptTerms: (Boolean) -> Unit,
    onAcceptPrivacy: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_before_continue),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = hasAcceptedTerms,
                    onCheckedChange = onAcceptTerms
                )
                Text(
                    text = stringResource(R.string.onboarding_agree_terms),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = hasAcceptedPrivacy,
                    onCheckedChange = onAcceptPrivacy
                )
                Text(
                    text = stringResource(R.string.onboarding_agree_privacy),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

