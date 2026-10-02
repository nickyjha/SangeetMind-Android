package com.sangeetmind.core.common

object Constants {
    // API Configuration
    const val BASE_URL = "https://sangeetmind-backend.fly.dev/"
    const val API_TIMEOUT_SECONDS = 30L

    // Public website (legal pages). Change here once the final domain is confirmed.
    const val WEBSITE_BASE_URL = "https://astrogeet.com"
    const val TERMS_URL = "$WEBSITE_BASE_URL/terms"
    const val PRIVACY_URL = "$WEBSITE_BASE_URL/privacy"
    const val DELETE_ACCOUNT_URL = "$WEBSITE_BASE_URL/delete-account"

    // Preferences Keys
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_REFRESH_TOKEN = "refresh_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_LANGUAGE = "language"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    const val PREF_PLAYBACK_QUALITY = "playback_quality"

    // Audio Cache
    const val AUDIO_CACHE_SIZE_MB = 500L
    const val AUDIO_CACHE_DIR = "audio_cache"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "sangeetmind_playback"
    // Channel name/description are string resources (core/audio R.string.audio_channel_*).
    const val NOTIFICATION_ID = 1001

    // Languages
    const val LANG_ENGLISH = "en"
    const val LANG_HINDI = "hi"

    // Feature Flags (TODO: Move to remote config)
    const val FEATURE_AI_RECOMMENDATIONS = false
    const val FEATURE_AUDIO_GENERATION = false
}

