package com.sangeetmind.features.auth.ui

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetmind.core.common.language.findActivity
import com.sangeetmind.core.ui.R as CoreR
import com.sangeetmind.core.ui.language.LanguagePickerAction
import com.sangeetmind.features.auth.AuthMode
import com.sangeetmind.features.auth.AuthUiState
import com.sangeetmind.features.auth.AuthViewModel
import com.sangeetmind.features.auth.PhoneAuthHelpers
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
        // The signup route is only reached from the email form's "Sign up" link,
        // so land with the email form already open.
        if (isSignup) viewModel.setEmailFormExpanded(true)
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
    // Firebase phone verification and Credential Manager both need the hosting Activity.
    val context = LocalContext.current
    val activity: Activity? = remember(context) { context.findActivity() }

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
                AuthMode.PHONE -> stringResource(R.string.auth_subtitle_phone)
                AuthMode.PHONE_OTP -> stringResource(R.string.auth_subtitle_otp)
                AuthMode.PHONE_NAME -> stringResource(R.string.auth_subtitle_name)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // NOTE: branch with when/if-else only — an early `return` mid-composition crashes.
        when (uiState.mode) {
            AuthMode.FORGOT_PASSWORD -> ForgotPasswordContent(
                email = uiState.email,
                isLoading = uiState.isLoading,
                error = uiState.error,
                resetEmailSent = uiState.resetEmailSent,
                onEmailChange = viewModel::onEmailChange,
                onSubmit = viewModel::sendPasswordReset,
                onBackToLogin = { viewModel.setMode(AuthMode.LOGIN) }
            )

            AuthMode.PHONE -> PhoneNumberContent(
                countryCode = uiState.countryCode,
                phoneNumber = uiState.phoneNumber,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onCountryCodeChange = viewModel::onCountryCodeChange,
                onPhoneNumberChange = viewModel::onPhoneNumberChange,
                onSendOtp = { activity?.let(viewModel::sendOtp) },
                onCancel = viewModel::cancelPhoneSignIn
            )

            AuthMode.PHONE_OTP -> OtpContent(
                otp = uiState.otp,
                sentTo = uiState.otpSentTo,
                resendSeconds = uiState.resendSeconds,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onOtpChange = viewModel::onOtpChange,
                onVerify = viewModel::verifyOtp,
                onResend = { activity?.let(viewModel::resendOtp) },
                onChangeNumber = viewModel::backToPhoneNumber
            )

            AuthMode.PHONE_NAME -> NameContent(
                name = uiState.name,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onNameChange = viewModel::onNameChange,
                onSubmit = viewModel::savePhoneName
            )

            AuthMode.LOGIN, AuthMode.SIGNUP -> SignInChooserContent(
                uiState = uiState,
                viewModel = viewModel,
                activity = activity,
                onToggleMode = onToggleMode
            )
        }
    }
}

/** LOGIN / SIGNUP: phone + Google buttons, then the collapsible email form. */
@Composable
private fun ColumnScope.SignInChooserContent(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    activity: Activity?,
    onToggleMode: () -> Unit
) {
    val isSignupMode = uiState.mode == AuthMode.SIGNUP
    val anyLoading = uiState.isLoading || uiState.isGoogleLoading

    Button(
        onClick = viewModel::startPhoneSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !anyLoading
    ) {
        Icon(Icons.Default.Phone, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(stringResource(R.string.auth_continue_phone))
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = { activity?.let(viewModel::signInWithGoogle) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !anyLoading && activity != null
    ) {
        if (uiState.isGoogleLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            GoogleMark()
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(R.string.auth_continue_google))
        }
    }

    // Errors from the Google path show here; once the email form is open they show inside it.
    if (uiState.error != null && !uiState.emailFormExpanded) {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorCard(uiState.error)
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(
        onClick = { viewModel.setEmailFormExpanded(!uiState.emailFormExpanded) },
        enabled = !anyLoading
    ) {
        Text(
            stringResource(
                if (uiState.emailFormExpanded) R.string.auth_hide_email_form else R.string.auth_or_use_email
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (uiState.emailFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
        )
    }

    if (uiState.emailFormExpanded) {
        Spacer(modifier = Modifier.height(8.dp))

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
            ErrorCard(uiState.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (isSignupMode) viewModel.signup() else viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !anyLoading
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

/** Simple "G" badge so the Google button is recognisable without bundling a logo asset. */
@Composable
private fun GoogleMark() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ColumnScope.PhoneNumberContent(
    countryCode: String,
    phoneNumber: String,
    isLoading: Boolean,
    error: String?,
    onCountryCodeChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        OutlinedTextField(
            value = countryCode,
            onValueChange = onCountryCodeChange,
            label = { Text(stringResource(R.string.auth_country_code)) },
            prefix = { Text("+") },
            modifier = Modifier.width(104.dp),
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text(stringResource(R.string.auth_phone_number)) },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSendOtp() })
        )
    }

    if (error != null) {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorCard(error)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSendOtp,
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
            Text(stringResource(R.string.auth_send_otp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = onCancel, enabled = !isLoading) {
        Text(stringResource(R.string.auth_other_ways))
    }
}

@Composable
private fun ColumnScope.OtpContent(
    otp: String,
    sentTo: String?,
    resendSeconds: Int,
    isLoading: Boolean,
    error: String?,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onChangeNumber: () -> Unit
) {
    if (sentTo != null) {
        Text(
            text = stringResource(R.string.auth_otp_sent_to, PhoneAuthHelpers.formatForDisplay(sentTo)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    OutlinedTextField(
        value = otp,
        onValueChange = onOtpChange,
        label = { Text(stringResource(R.string.auth_otp)) },
        leadingIcon = { Icon(Icons.Default.Sms, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onVerify() })
    )

    if (error != null) {
        Spacer(modifier = Modifier.height(16.dp))
        ErrorCard(error)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onVerify,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading && PhoneAuthHelpers.isValidOtp(otp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(stringResource(R.string.auth_verify_otp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onResend, enabled = !isLoading && resendSeconds == 0) {
            Text(
                if (resendSeconds > 0) stringResource(R.string.auth_resend_in, resendSeconds)
                else stringResource(R.string.auth_resend_otp)
            )
        }
        TextButton(onClick = onChangeNumber, enabled = !isLoading) {
            Text(stringResource(R.string.auth_change_number))
        }
    }
}

/** Asked once after a phone / Google sign-in when Firebase has no display name for the user. */
@Composable
private fun ColumnScope.NameContent(
    name: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.auth_name)) },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
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
            Text(stringResource(R.string.auth_continue))
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
    } else {
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
