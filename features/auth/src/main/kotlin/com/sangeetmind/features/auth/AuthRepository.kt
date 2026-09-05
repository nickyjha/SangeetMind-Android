package com.sangeetmind.features.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sangeetmind.core.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(firebaseAuth.signInWithEmailAndPassword(email, password))
                val user = result.user ?: return@withContext Result.Error(
                    IllegalStateException("No user returned"), "Sign in failed"
                )
                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Sign in failed")
            }
        }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            try {
                val result = Tasks.await(firebaseAuth.createUserWithEmailAndPassword(email, password))
                val user = result.user ?: return@withContext Result.Error(
                    IllegalStateException("No user returned"), "Sign up failed"
                )
                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Sign up failed")
            }
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Tasks.await(firebaseAuth.sendPasswordResetEmail(email))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Could not send reset email")
            }
        }

    suspend fun confirmPasswordReset(code: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Tasks.await(firebaseAuth.confirmPasswordReset(code, newPassword))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, e.message ?: "Could not reset password")
            }
        }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
