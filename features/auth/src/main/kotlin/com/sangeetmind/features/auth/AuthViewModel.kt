package com.sangeetmind.features.auth

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LOGIN / SIGNUP / FORGOT_PASSWORD are the email flows.
 * PHONE → PHONE_OTP → (PHONE_NAME if the Firebase user has no display name yet) is the OTP flow.
 */
enum class AuthMode { LOGIN, SIGNUP, FORGOT_PASSWORD, PHONE, PHONE_OTP, PHONE_NAME }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    // "or use email" disclosure on the LOGIN / SIGNUP screen
    val emailFormExpanded: Boolean = false,
    // Google
    val isGoogleLoading: Boolean = false,
    // Phone OTP
    val countryCode: String = PhoneAuthHelpers.DEFAULT_COUNTRY_CODE,
    val phoneNumber: String = "",
    val otp: String = "",
    /** E.164 number the OTP was sent to (shown on the OTP screen). */
    val otpSentTo: String? = null,
    /** Seconds until "Resend OTP" is enabled again; 0 = enabled. */
    val resendSeconds: Int = 0
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.currentUser != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Phone verification session (not UI state; survives recomposition because the VM does)
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var otpSentAtMillis: Long = 0L
    private var countdownJob: Job? = null

    private fun str(@StringRes id: Int): String =
        appContext.withAppLanguage(languageManager.current).getString(id)

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    /** LOGIN or SIGNUP — where "Other ways to sign in" returns to from the phone flow. */
    private var homeMode: AuthMode = AuthMode.LOGIN

    fun setMode(mode: AuthMode) {
        if (mode == AuthMode.LOGIN || mode == AuthMode.SIGNUP) homeMode = mode
        _uiState.update { it.copy(mode = mode, error = null, resetEmailSent = false) }
    }

    fun setEmailFormExpanded(expanded: Boolean) {
        _uiState.update { it.copy(emailFormExpanded = expanded, error = null) }
    }

    fun login() {
        val state = _uiState.value

        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = str(R.string.auth_error_password_short)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(state.email, state.password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_sign_in_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun signup() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.auth_error_name_required)) }
            return
        }
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = str(R.string.auth_error_password_short)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(state.email, state.password)) {
                is Result.Success -> {
                    // Best effort: the account exists either way, so a failed profile write
                    // should not block the person from getting in.
                    authRepository.updateDisplayName(state.name)
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, error = null) }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_sign_up_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        if (!validateEmail(state.email)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_email)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendPasswordReset(state.email)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_reset_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    // ---- Google -------------------------------------------------------------------------------

    fun signInWithGoogle(activity: Activity) {
        if (_uiState.value.isGoogleLoading || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, error = null) }
            when (val result = authRepository.signInWithGoogle(activity)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isGoogleLoading = false) }
                    onSignedIn(result.data)
                }
                is Result.Error -> {
                    val message = when (result.exception) {
                        is SignInCancelledException -> null // person dismissed the sheet
                        is GoogleSignInNotConfiguredException -> str(R.string.auth_error_google_not_configured)
                        else -> result.message ?: str(R.string.auth_error_google_failed)
                    }
                    _uiState.update { it.copy(isGoogleLoading = false, error = message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // ---- Phone OTP ----------------------------------------------------------------------------

    fun startPhoneSignIn() {
        _uiState.update { it.copy(mode = AuthMode.PHONE, error = null, otp = "") }
    }

    fun onCountryCodeChange(input: String) {
        val cc = PhoneAuthHelpers.sanitizeCountryCodeInput(input)
        _uiState.update {
            it.copy(
                countryCode = cc,
                phoneNumber = PhoneAuthHelpers.sanitizePhoneInput(it.phoneNumber, cc),
                error = null
            )
        }
    }

    fun onPhoneNumberChange(input: String) {
        _uiState.update {
            it.copy(phoneNumber = PhoneAuthHelpers.sanitizePhoneInput(input, it.countryCode), error = null)
        }
    }

    fun onOtpChange(input: String) {
        _uiState.update { it.copy(otp = PhoneAuthHelpers.sanitizeOtpInput(input), error = null) }
    }

    /** "Send OTP" from the number screen. [activity] is required by Firebase for reCAPTCHA / Play Integrity. */
    fun sendOtp(activity: Activity) {
        val state = _uiState.value
        if (state.isLoading) return
        val e164 = PhoneAuthHelpers.normalizeToE164(state.countryCode, state.phoneNumber)
        if (e164 == null) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_phone)) }
            return
        }
        // A fresh number gets a fresh session; the resend token only applies to the same number.
        if (e164 != state.otpSentTo) {
            verificationId = null
            resendToken = null
        }
        _uiState.update { it.copy(isLoading = true, error = null, otp = "") }
        authRepository.startPhoneVerification(activity, e164, phoneCallbacks(e164), resendToken = null)
    }

    /** "Resend OTP" from the OTP screen; no-op while the countdown is running. */
    fun resendOtp(activity: Activity) {
        val state = _uiState.value
        val e164 = state.otpSentTo ?: return
        if (state.isLoading) return
        if (!PhoneAuthHelpers.canResend(otpSentAtMillis, System.currentTimeMillis())) return
        _uiState.update { it.copy(isLoading = true, error = null, otp = "") }
        authRepository.startPhoneVerification(activity, e164, phoneCallbacks(e164), resendToken)
    }

    /** "Verify" from the OTP screen. */
    fun verifyOtp() {
        val state = _uiState.value
        if (state.isLoading) return
        if (!PhoneAuthHelpers.isValidOtp(state.otp)) {
            _uiState.update { it.copy(error = str(R.string.auth_error_invalid_otp)) }
            return
        }
        val id = verificationId
        if (id == null) {
            _uiState.update { it.copy(error = str(R.string.auth_error_otp_expired)) }
            return
        }
        signInWithPhoneCredential(authRepository.phoneCredential(id, state.otp))
    }

    /** "Change number" / back from the OTP or name screen. */
    fun backToPhoneNumber() {
        cancelCountdown()
        _uiState.update { it.copy(mode = AuthMode.PHONE, error = null, otp = "", isLoading = false) }
    }

    /** Leaves the phone flow entirely and returns to the login screen. */
    fun cancelPhoneSignIn() {
        cancelCountdown()
        verificationId = null
        resendToken = null
        _uiState.update {
            it.copy(mode = homeMode, error = null, otp = "", otpSentTo = null, isLoading = false)
        }
    }

    /** Saves the name asked for once after a first phone sign-in, then finishes. */
    fun savePhoneName() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = str(R.string.auth_error_name_required)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.updateDisplayName(state.name)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message ?: str(R.string.auth_error_name_save_failed))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun phoneCallbacks(e164: String) =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant verification or SMS auto-retrieval: show the code if we have it, then sign in.
                credential.smsCode?.let { code ->
                    _uiState.update { it.copy(otp = PhoneAuthHelpers.sanitizeOtpInput(code)) }
                }
                cancelCountdown()
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val message = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> str(R.string.auth_error_invalid_phone)
                    is FirebaseTooManyRequestsException -> str(R.string.auth_error_too_many_requests)
                    else -> e.message ?: str(R.string.auth_error_phone_failed)
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                resendToken = token
                _uiState.update {
                    it.copy(mode = AuthMode.PHONE_OTP, isLoading = false, error = null, otpSentTo = e164)
                }
                startCountdown()
            }

            override fun onCodeAutoRetrievalTimeOut(id: String) {
                // The person can still type the code manually; nothing to do.
            }
        }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithCredential(credential)) {
                is Result.Success -> {
                    cancelCountdown()
                    _uiState.update { it.copy(isLoading = false) }
                    onSignedIn(result.data)
                }
                is Result.Error -> {
                    val message = when (result.exception) {
                        is FirebaseAuthInvalidCredentialsException -> str(R.string.auth_error_invalid_otp)
                        is FirebaseTooManyRequestsException -> str(R.string.auth_error_too_many_requests)
                        else -> result.message ?: str(R.string.auth_error_phone_failed)
                    }
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    /** Common landing for Google and phone: ask for a name once if Firebase has none. */
    private fun onSignedIn(user: FirebaseUser) {
        verificationId = null
        resendToken = null
        if (user.displayName.isNullOrBlank()) {
            _uiState.update { it.copy(mode = AuthMode.PHONE_NAME, error = null) }
        } else {
            _uiState.update { it.copy(isAuthenticated = true, error = null) }
        }
    }

    private fun startCountdown() {
        otpSentAtMillis = System.currentTimeMillis()
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = PhoneAuthHelpers.resendSecondsRemaining(otpSentAtMillis, System.currentTimeMillis())
                _uiState.update { it.copy(resendSeconds = remaining) }
                if (remaining == 0) break
                delay(1_000)
            }
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        otpSentAtMillis = 0L
        _uiState.update { it.copy(resendSeconds = 0) }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
