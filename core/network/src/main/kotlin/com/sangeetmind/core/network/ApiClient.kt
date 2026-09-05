package com.sangeetmind.core.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.sangeetmind.core.common.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    @BaseOkHttpClient
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Attaches the current Firebase user's ID token as a Bearer header (backend verifies
     * it via Firebase Admin, see verify_firebase_user). Runs off the main thread — OkHttp
     * interceptors always execute on the calling (background) dispatcher thread — so
     * blocking on the token Task here is the standard Firebase+OkHttp pattern.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth): Interceptor =
        Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            if (BuildConfig.CHART_API_KEY.isNotEmpty()) {
                builder.header("x-api-key", BuildConfig.CHART_API_KEY)
            }

            val user = firebaseAuth.currentUser
            var idToken = user?.let { runCatching { Tasks.await(it.getIdToken(false)).token }.getOrNull() }
            if (idToken != null) {
                builder.header("Authorization", "Bearer $idToken")
            }

            val response = chain.proceed(builder.build())

            if (response.code == 401 && user != null) {
                response.close()
                idToken = runCatching { Tasks.await(user.getIdToken(true)).token }.getOrNull()
                val retryBuilder = original.newBuilder()
                if (BuildConfig.CHART_API_KEY.isNotEmpty()) {
                    retryBuilder.header("x-api-key", BuildConfig.CHART_API_KEY)
                }
                if (idToken != null) {
                    retryBuilder.header("Authorization", "Bearer $idToken")
                }
                chain.proceed(retryBuilder.build())
            } else {
                response
            }
        }

    @Provides
    @Singleton
    @AuthInterceptorOkHttpClient
    fun provideAuthOkHttpClient(
        @BaseOkHttpClient baseClient: OkHttpClient,
        authInterceptor: Interceptor
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(authInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        @AuthInterceptorOkHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    /**
     * Nominatim's usage policy requires a descriptive User-Agent — no API key involved.
     * Deliberately built on [provideBaseOkHttpClient] (no Authorization/x-api-key) so
     * SangeetMind backend credentials are never sent to this third-party host.
     */
    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(
        @BaseOkHttpClient baseClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        val client = baseClient.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "SangeetMind-Android/1.0")
                        .build()
                )
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideNominatimApi(@GeocodingRetrofit retrofit: Retrofit): NominatimApi =
        retrofit.create(NominatimApi::class.java)

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideKundliApi(retrofit: Retrofit): KundliApi = retrofit.create(KundliApi::class.java)

    @Provides
    @Singleton
    fun provideAstrologyApi(retrofit: Retrofit): AstrologyApi = retrofit.create(AstrologyApi::class.java)

    @Provides
    @Singleton
    fun provideHoroscopeApi(retrofit: Retrofit): HoroscopeApi = retrofit.create(HoroscopeApi::class.java)

    @Provides
    @Singleton
    fun providePanchangApi(retrofit: Retrofit): PanchangApi = retrofit.create(PanchangApi::class.java)

    @Provides
    @Singleton
    fun provideMuhuratApi(retrofit: Retrofit): MuhuratApi = retrofit.create(MuhuratApi::class.java)

    @Provides
    @Singleton
    fun provideMatchApi(retrofit: Retrofit): MatchApi = retrofit.create(MatchApi::class.java)

    @Provides
    @Singleton
    fun provideNumerologyApi(retrofit: Retrofit): NumerologyApi = retrofit.create(NumerologyApi::class.java)

    @Provides
    @Singleton
    fun provideInterpretationApi(retrofit: Retrofit): InterpretationApi =
        retrofit.create(InterpretationApi::class.java)
}
