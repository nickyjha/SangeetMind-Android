package com.sangeetmind.features.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LanguagePickerAction
import com.sangeetmind.features.auth.AuthMode
import com.sangeetmind.features.auth.AuthUiState
import com.sangeetmind.features.auth.AuthViewModel
import com.sangeetmind.features.auth.R

@Composable
fun AuthScreen(
    isSignup: Boolean = false,
    onAuthSuccess: () -> Unit = {},
    onToggleMode: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isSignup) {
        viewModel.setMode(if (isSignup) AuthMode.SIGNUP else AuthMode.LOGIN)
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthContent(
            uiState = uiState,
            viewModel = viewModel,
            onToggleMode = onToggleMode
        )
        LanguagePickerAction(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        )
    }
}

@Composable
private fun AuthContent(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onToggleMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(CoreR.string.common_app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (uiState.mode) {
                AuthMode.SIGNUP -> stringResource(R.string.auth_subtitle_signup)
                AuthMode.FORGOT_PASSWORD -> stringResource(R.string.auth_subtitle_forgot)
                AuthMode.LOGIN -> stringResource(R.string.auth_subtitle_login)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.mode == AuthMode.FORGOT_PASSWORD) {
            ForgotPasswordContent(
                email = uiState.email,
                isLoading = uiState.isLoading,
                error = uiState.error,
                resetEmailSent = uiState.resetEmailSent,
                onEmailChange = viewModel::onEmailChange,
                onSubmit = viewModel::sendPasswordReset,
                onBackToLogin = { viewModel.setMode(AuthMode.LOGIN) }
            )
            return@Column
        }

        val isSignupMode = uiState.mode == AuthMode.SIGNUP

        if (isSignupMode) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.auth_name)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.auth_email)) },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.auth_password)) },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.auth_hide_password else R.string.auth_show_password
                        )
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (isSignupMode) viewModel.signup() else viewModel.login() }
            )
        )

        if (!isSignupMode) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.setMode(AuthMode.FORGOT_PASSWORD) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.auth_forgot_password))
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ErrorCard(uiState.error!!)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (isSignupMode) viewModel.signup() else viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(if (isSignupMode) R.string.auth_sign_up else R.string.auth_log_in))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onToggleMode) {
            Text(
                text = stringResource(
                    if (isSignupMode) R.string.auth_have_account else R.string.auth_no_account
                )
            )
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    email: String,
    isLoading: Boolean,
    error: String?,
    resetEmailSent: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    if (resetEmailSent) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.auth_reset_sent),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBackToLogin) { Text(stringResource(R.string.auth_back_to_login)) }
        return
    }

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.auth_email)) },
        leadingIcon = { Icon(Icons.Default.Email, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() })
    )

    if (error != null) {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorCard(error)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(stringResource(R.string.auth_send_reset))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = onBackToLogin) { Text(stringResource(R.string.auth_back_to_login)) }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
