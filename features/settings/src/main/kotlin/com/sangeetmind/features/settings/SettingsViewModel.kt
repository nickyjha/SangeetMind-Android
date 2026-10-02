package com.sangeetmind.features.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.sangeetmind.core.common.theme.ThemeManager
import com.sangeetmind.core.common.theme.ThemePreference
import com.sangeetmind.core.network.UsersApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val darkMode: DarkModePreference = DarkModePreference.LIGHT,
    val deleteAccount: DeleteAccountState = DeleteAccountState.Idle,
    /** True once [SettingsViewModel.signOut] has signed the user out. */
    val signedOut: Boolean = false
)

/** Progress of the "Delete account" flow (Settings danger zone). */
sealed interface DeleteAccountState {
    data object Idle : DeleteAccountState
    data object Deleting : DeleteAccountState

    /** Server data gone, Firebase user gone (or already gone), signed out. */
    data object Done : DeleteAccountState

    /**
     * Server data is gone and we signed out, but Firebase refused to delete the auth
     * user because the session is too old. The user must sign in again and delete.
     */
    data object SignInAgain : DeleteAccountState

    /** The backend call failed; nothing was deleted. */
    data object Failed : DeleteAccountState
}

enum class DarkModePreference {
    LIGHT, DARK, SYSTEM;

    fun toTheme(): ThemePreference = when (this) {
        LIGHT -> ThemePreference.LIGHT
        DARK -> ThemePreference.DARK
        SYSTEM -> ThemePreference.SYSTEM
    }

    companion object {
        fun from(theme: ThemePreference): DarkModePreference = when (theme) {
            ThemePreference.LIGHT -> LIGHT
            ThemePreference.DARK -> DARK
            ThemePreference.SYSTEM -> SYSTEM
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val usersApi: UsersApi,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(darkMode = DarkModePreference.from(themeManager.preference.value))
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkMode(preference: DarkModePreference) {
        _uiState.update { it.copy(darkMode = preference) }
        themeManager.setPreference(preference.toTheme())
    }

    /** Signs out of Firebase (same as AuthRepository.signOut); the screen then leaves. */
    fun signOut() {
        firebaseAuth.signOut()
        _uiState.update { it.copy(signedOut = true) }
    }

    /**
     * Google Play account-deletion flow:
     * 1. `DELETE /v1/users/me` wipes server data (kundlis, readings, wallet, Premium ...).
     * 2. Delete the Firebase user client-side. Firebase requires a recent sign-in for
     *    this; if it refuses we still sign out and ask the user to sign in and retry.
     * 3. Sign out so the app lands on the auth screen.
     *
     * The backend also deletes the Firebase user best-effort, so a client-side
     * "user not found" is treated as success.
     */
    fun deleteAccount() {
        if (_uiState.value.deleteAccount == DeleteAccountState.Deleting) return
        _uiState.update { it.copy(deleteAccount = DeleteAccountState.Deleting) }
        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                try {
                    usersApi.deleteMyAccount()
                } catch (e: Exception) {
                    Log.w(TAG, "Account deletion request failed", e)
                    return@withContext DeleteAccountState.Failed
                }
                val user = firebaseAuth.currentUser
                val state = if (user == null) {
                    DeleteAccountState.Done
                } else {
                    try {
                        Tasks.await(user.delete())
                        DeleteAccountState.Done
                    } catch (e: Exception) {
                        val cause = (e as? java.util.concurrent.ExecutionException)?.cause ?: e
                        if (cause is FirebaseAuthRecentLoginRequiredException) {
                            DeleteAccountState.SignInAgain
                        } else {
                            // Typically "user not found": the backend already removed it.
                            Log.w(TAG, "Firebase user delete failed, signing out", cause)
                            DeleteAccountState.Done
                        }
                    }
                }
                firebaseAuth.signOut()
                state
            }
            _uiState.update { it.copy(deleteAccount = outcome) }
        }
    }

    /** Resets the delete flow after the screen has shown the error. */
    fun dismissDeleteAccountError() {
        _uiState.update { it.copy(deleteAccount = DeleteAccountState.Idle) }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}

