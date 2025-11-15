package com.sangeetmind.core.network

import com.sangeetmind.libs.models.*
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): AuthToken

    // Raag endpoints
    @GET("raag")
    suspend fun getRaags(
        @Query("filter") filter: String? = null,
        @Query("timeOfDay") timeOfDay: String? = null,
        @Query("mood") mood: String? = null,
        @Query("intensity") intensity: String? = null
    ): List<Raag>

    @GET("raag/{id}")
    suspend fun getRaagById(@Path("id") id: String): Raag

    @GET("raag/search")
    suspend fun searchRaags(@Query("q") query: String): List<Raag>

    // Meditation endpoints
    @GET("meditation/sessions")
    suspend fun getMeditationSessions(): List<MeditationSession>

    @GET("meditation/sessions/{id}")
    suspend fun getMeditationSessionById(@Path("id") id: String): MeditationSession

    // Astrology endpoints
    @POST("astrology/generate")
    suspend fun generateAstrologyRecommendation(
        @Body profile: AstrologyProfile
    ): AstrologyRecommendation

    // User endpoints
    @GET("user/profile")
    suspend fun getUserProfile(): User

    @PUT("user/preferences")
    suspend fun updateUserPreferences(@Body preferences: UserPreferences): User
}

