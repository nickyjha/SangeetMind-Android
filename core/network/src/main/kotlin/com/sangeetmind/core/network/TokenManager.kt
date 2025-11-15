package com.sangeetmind.core.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication tokens
 * TODO: Integrate with DataStore for persistent storage
 */
@Singleton
class TokenManager @Inject constructor() {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun setTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    fun getAccessToken(): String? = accessToken

    fun getRefreshToken(): String? = refreshToken

    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    fun hasValidToken(): Boolean = !accessToken.isNullOrEmpty()
}

