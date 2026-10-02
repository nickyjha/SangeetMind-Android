package com.sangeetmind.features.auth

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.sangeetmind.core.common.Result
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import com.sangeetmind.core.network.PushTokenRegistrar
import com.sangeetmind.core.network.friendlyErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Thrown when the person dismissed the Google account picker; callers should stay silent. */
class SignInCancelledException : Exception("Sign-in cancelled")

/** Thrown when `default_web_client_id` is missing (google-services.json has no web client). */
class GoogleSignInNotConfiguredException : Exception("Google sign-in is not configured")

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val pushTokenRegistrar: PushTokenRegistrar,
    @ApplicationContext private val appContext: Context,
    private val languageManager: LanguageManager
) {
    private fun localized(): Context = appContext.withAppLanguage(languageManager.current)

    private fun str(@StringRes id: Int): String = localized().getString(id)

    /**
     * A user-facing message for a Firebase/Credential Manager failure, in the app language.
     * Known Firebase cases get specific text; anything else goes through the shared
     * [friendlyErrorMessage] so raw English SDK messages never reach the screen.
     */
    private fun friendly(
        e: Throwable,
        @StringRes fallback: Int,
        @StringRes invalidCredentials: Int = R.string.auth_error_wrong_credentials
    ): String {
        val cause = (e as? ExecutionException)?.cause ?: e
        return when (cause) {
            is FirebaseNetworkException -> localized().getString(com.sangeetmind.core.network.R.string.net_err_offline)
            is FirebaseTooManyRequestsException -> str(R.string.auth_error_too_many_requests)
            is FirebaseAuthWeakPasswordException -> str(R.string.auth_error_password_short)
            is FirebaseAuthUserCollisionException -> str(R.string.auth_error_email_in_use)
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> str(invalidCredentials)
            else -> friendlyErrorMessage(cause, localized(), str(fallback))
        }
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(firebaseAuth.signInWithEmailAndPassword(email, password))
                val user = result.user ?: return@withContext Result.Error(
                    IllegalStateException("No user returned"), str(R.string.auth_error_sign_in_failed)
                )
                pushTokenRegistrar.registerCurrentToken()
                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(e, friendly(e, R.string.auth_error_sign_in_failed))
            }
        }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(firebaseAuth.createUserWithEmailAndPassword(email, password))
                val user = result.user ?: return@withContext Result.Error(
                    IllegalStateException("No user returned"), str(R.string.auth_error_sign_up_failed)
                )
                pushTokenRegistrar.registerCurrentToken()
                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(e, friendly(e, R.string.auth_error_sign_up_failed))
            }
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Tasks.await(firebaseAuth.sendPasswordResetEmail(email))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, friendly(e, R.string.auth_error_reset_failed))
            }
        }

    suspend fun confirmPasswordReset(code: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Tasks.await(firebaseAuth.confirmPasswordReset(code, newPassword))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, friendly(e, R.string.auth_error_reset_failed))
            }
        }

    /** Saves the display name on the signed-in Firebase user (used by signup and phone sign-in). */
    suspend fun updateDisplayName(name: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser ?: return@withContext Result.Error(
                    IllegalStateException("No signed-in user"), str(R.string.auth_error_name_save_failed)
                )
                val request = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                Tasks.await(user.updateProfile(request))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, friendly(e, R.string.auth_error_name_save_failed))
            }
        }

    // ---- Phone OTP ---------------------------------------------------------------------------

    /**
     * Starts Firebase phone verification. Firebase needs the hosting [activity] for the
     * reCAPTCHA / Play Integrity fallback, and all [callbacks] fire on the main thread.
     * Pass [resendToken] (from a previous `onCodeSent`) to force an SMS resend.
     */
    fun startPhoneVerification(
        activity: Activity,
        phoneE164: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .apply { if (resendToken != null) setForceResendingToken(resendToken) }
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun phoneCredential(verificationId: String, smsCode: String): PhoneAuthCredential =
        PhoneAuthProvider.getCredential(verificationId, smsCode)

    /** Signs in with any [AuthCredential] (phone OTP credential or Google ID token credential). */
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(firebaseAuth.signInWithCredential(credential))
                val user = result.user ?: return@withContext Result.Error(
                    IllegalStateException("No user returned"), str(R.string.auth_error_sign_in_failed)
                )
                pushTokenRegistrar.registerCurrentToken()
                Result.Success(user)
            } catch (e: Exception) {
                val wrongCode = if (credential is PhoneAuthCredential) {
                    R.string.auth_error_invalid_otp
                } else {
                    R.string.auth_error_google_failed
                }
                Result.Error(e, friendly(e, R.string.auth_error_sign_in_failed, wrongCode))
            }
        }

    // ---- Google (Credential Manager) ---------------------------------------------------------

    /**
     * Shows the Sign-in-with-Google bottom sheet via Credential Manager and exchanges the
     * returned ID token for a Firebase session.
     *
     * The web client id is the `client_type: 3` OAuth client in google-services.json; the
     * google-services plugin writes it to the app's `default_web_client_id` string resource.
     * That R class belongs to :app, so a library module has to look it up by name.
     *
     * Returns [Result.Error] carrying [SignInCancelledException] when the sheet is dismissed —
     * the caller should not show an error for that.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        val webClientId = webClientId(activity)
            ?: return Result.Error(GoogleSignInNotConfiguredException())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            // Show every Google account on the device, not only ones previously used here —
            // first-time users have no "authorized" account yet.
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val idToken: String = try {
            val response = CredentialManager.create(activity).getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                return Result.Error(
                    IllegalStateException("Unexpected credential type: ${credential.type}"),
                    str(R.string.auth_error_google_failed)
                )
            }
        } catch (e: GetCredentialCancellationException) {
            return Result.Error(SignInCancelledException())
        } catch (e: NoCredentialException) {
            // No Google account on the device (or Play Services unavailable).
            return Result.Error(e, str(R.string.auth_error_no_google_account))
        } catch (e: GetCredentialException) {
            return Result.Error(e, friendly(e, R.string.auth_error_google_failed))
        } catch (e: GoogleIdTokenParsingException) {
            return Result.Error(e, str(R.string.auth_error_google_failed))
        }

        return signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    }

    private fun webClientId(activity: Activity): String? {
        val resId = activity.resources.getIdentifier(
            "default_web_client_id", "string", activity.packageName
        )
        if (resId == 0) return null
        return activity.getString(resId).takeIf { it.isNotBlank() }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
