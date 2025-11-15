package com.sangeetmind.integration.backendstub

import com.sangeetmind.libs.models.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock API service for local development and testing
 * Provides sample data without requiring a backend server
 */
@Singleton
class MockApiService @Inject constructor() {

    fun getMockRaags(): List<Raag> = listOf(
        Raag(
            id = "raag_1",
            name = "Bhairav",
            nameHindi = "भैरव",
            description = "A morning raag that evokes a serious and devotional mood",
            artworkUrl = "https://picsum.photos/seed/bhairav/400/400",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationSeconds = 360,
            tags = listOf("classical", "morning", "devotional"),
            timeOfDay = TimeOfDay.MORNING,
            mood = Mood.DEVOTIONAL,
            intensity = Intensity.HIGH
        ),
        Raag(
            id = "raag_2",
            name = "Yaman",
            nameHindi = "यमन",
            description = "An evening raag known for its peaceful and romantic character",
            artworkUrl = "https://picsum.photos/seed/yaman/400/400",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationSeconds = 420,
            tags = listOf("classical", "evening", "romantic"),
            timeOfDay = TimeOfDay.EVENING,
            mood = Mood.ROMANTIC,
            intensity = Intensity.MEDIUM
        ),
        Raag(
            id = "raag_3",
            name = "Darbari Kanada",
            nameHindi = "दरबारी कानड़ा",
            description = "A late night raag with a deep, meditative quality",
            artworkUrl = "https://picsum.photos/seed/darbari/400/400",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationSeconds = 480,
            tags = listOf("classical", "night", "meditative"),
            timeOfDay = TimeOfDay.NIGHT,
            mood = Mood.PEACEFUL,
            intensity = Intensity.LOW
        ),
        Raag(
            id = "raag_4",
            name = "Bhupali",
            nameHindi = "भूपाली",
            description = "A soothing evening raag that brings peace and tranquility",
            artworkUrl = "https://picsum.photos/seed/bhupali/400/400",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationSeconds = 300,
            tags = listOf("classical", "evening", "peaceful"),
            timeOfDay = TimeOfDay.EVENING,
            mood = Mood.PEACEFUL,
            intensity = Intensity.LOW
        ),
        Raag(
            id = "raag_5",
            name = "Malkauns",
            nameHindi = "मालकौंस",
            description = "A midnight raag with a mysterious and introspective character",
            artworkUrl = "https://picsum.photos/seed/malkauns/400/400",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            durationSeconds = 390,
            tags = listOf("classical", "night", "introspective"),
            timeOfDay = TimeOfDay.NIGHT,
            mood = Mood.MELANCHOLIC,
            intensity = Intensity.MEDIUM
        )
    )

    fun getMockMeditationSessions(): List<MeditationSession> = listOf(
        MeditationSession(
            id = "med_1",
            title = "Morning Breath Meditation",
            titleHindi = "प्रातःकालीन श्वास ध्यान",
            description = "Start your day with focused breathing exercises",
            durationMinutes = 10,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            artworkUrl = "https://picsum.photos/seed/meditation1/400/400",
            category = MeditationCategory.BREATHING,
            isGuided = true
        ),
        MeditationSession(
            id = "med_2",
            title = "Stress Relief",
            titleHindi = "तनाव मुक्ति",
            description = "Release tension and find calm",
            durationMinutes = 15,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            artworkUrl = "https://picsum.photos/seed/meditation2/400/400",
            category = MeditationCategory.STRESS_RELIEF,
            isGuided = true
        )
    )

    fun getMockUser(): User = User(
        id = "user_1",
        email = "user@sangeetmind.com",
        name = "Test User",
        profileImageUrl = "https://picsum.photos/seed/user/200/200",
        preferences = UserPreferences(
            language = "en",
            playbackQuality = PlaybackQuality.HIGH,
            downloadOnWifiOnly = true,
            notificationsEnabled = true
        )
    )
}

@Module
@InstallIn(SingletonComponent::class)
object MockApiModule {
    @Provides
    @Singleton
    fun provideMockApiService(): MockApiService = MockApiService()
}

