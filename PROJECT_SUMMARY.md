# SangeetMind Android - Project Scaffold Summary

## Overview

This document provides a comprehensive overview of the scaffolded SangeetMind Android project, a production-ready mobile app for raag-based meditation and music with astrology personalization.

## Project Structure

### File Tree (First 6 Levels)

```
sangeetmind-android/
├── .github/
│   └── workflows/
│       └── ci.yml                          # GitHub Actions CI workflow
├── .gitignore                              # Git ignore rules
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar              # Gradle wrapper binary
│   │   └── gradle-wrapper.properties       # Gradle version config
│   └── libs.versions.toml                  # Centralized dependency versions
├── gradlew                                 # Gradle wrapper script (Unix)
├── gradlew.bat                             # Gradle wrapper script (Windows)
├── gradle.properties                       # Gradle and app properties
├── settings.gradle.kts                     # Multi-module project settings
├── build.gradle.kts                        # Root build configuration
├── LICENSE                                 # MIT License
├── README.md                               # Project documentation
├── CONTRIBUTING.md                         # Contribution guidelines
├── SECURITY.md                             # Security policy
├── PROJECT_SUMMARY.md                      # This file
│
├── app/                                    # Main application module
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/sangeetmind/app/
│       │   │   ├── SangeetMindApplication.kt    # Hilt app + Timber init
│       │   │   ├── MainActivity.kt              # Compose entry point
│       │   │   └── navigation/
│       │   │       └── SangeetMindNavHost.kt    # Navigation graph
│       │   └── res/
│       │       ├── values/
│       │       │   ├── strings.xml              # English strings
│       │       │   └── themes.xml
│       │       ├── values-hi/
│       │       │   └── strings.xml              # Hindi strings
│       │       └── xml/
│       │           └── backup_rules.xml
│       ├── test/
│       │   └── kotlin/com/sangeetmind/app/
│       │       └── ExampleUnitTest.kt
│       └── androidTest/
│           └── kotlin/com/sangeetmind/app/
│               └── HomeScreenTest.kt            # Compose UI test
│
├── core/
│   ├── common/                                  # Common utilities
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../core/common/
│   │       ├── Result.kt                        # Result wrapper
│   │       ├── Constants.kt                     # App constants
│   │       └── di/
│   │           └── DispatchersModule.kt         # Coroutine dispatchers
│   │
│   ├── ui/                                      # Shared Compose components
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── kotlin/.../core/ui/theme/
│   │       │   ├── Color.kt                     # Material3 colors
│   │       │   ├── Type.kt                      # Typography
│   │       │   └── Theme.kt                     # SangeetMindTheme
│   │       └── res/values/
│   │           └── strings.xml
│   │
│   ├── network/                                 # Retrofit + OkHttp
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../core/network/
│   │       ├── ApiClient.kt                     # Retrofit + interceptors
│   │       ├── TokenManager.kt                  # Auth token management
│   │       └── ApiService.kt                    # API endpoints
│   │
│   ├── database/                                # Room database
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../core/database/
│   │       ├── SangeetMindDatabase.kt
│   │       ├── Converters.kt                    # Type converters
│   │       ├── DatabaseModule.kt                # Hilt module
│   │       ├── dao/
│   │       │   └── RaagDao.kt
│   │       └── entity/
│   │           └── RaagEntity.kt
│   │
│   └── audio/                                   # ExoPlayer service
│       ├── build.gradle.kts
│       └── src/main/kotlin/.../core/audio/
│           ├── PlayerService.kt                 # Foreground service
│           ├── PlayerRepository.kt              # Playback control
│           └── AudioModule.kt                   # ExoPlayer + cache
│
├── features/
│   ├── onboarding/                              # Onboarding flows
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../features/onboarding/
│   │
│   ├── auth/                                    # Login/signup
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../features/auth/
│   │
│   ├── raaglibrary/                             # Raag browsing
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/kotlin/.../features/raaglibrary/
│   │       │   ├── RaagRepository.kt
│   │       │   ├── RaagListViewModel.kt
│   │       │   └── ui/
│   │       │       └── RaagListScreen.kt        # Compose UI
│   │       └── test/kotlin/.../features/raaglibrary/
│   │           └── RaagListViewModelTest.kt     # Unit tests
│   │
│   ├── player/                                  # Audio player UI
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../features/player/
│   │
│   ├── meditation/                              # Meditation sessions
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../features/meditation/
│   │
│   ├── astrology/                               # Birth chart input
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../features/astrology/
│   │
│   └── settings/                                # User preferences
│       ├── build.gradle.kts
│       └── src/main/kotlin/.../features/settings/
│
├── libs/
│   └── models/                                  # Domain models
│       ├── build.gradle.kts
│       └── src/main/kotlin/.../libs/models/
│           ├── Raag.kt
│           ├── User.kt
│           ├── AuthToken.kt
│           ├── MeditationSession.kt
│           └── AstrologyProfile.kt
│
└── integration/
    └── backend-stub/                            # Mock API
        ├── build.gradle.kts
        └── src/main/
            ├── kotlin/.../integration/backendstub/
            │   └── MockApiService.kt            # Sample data
            └── resources/mock/
```

## Key Components

### 1. Application Class

**File**: `app/src/main/kotlin/com/sangeetmind/app/SangeetMindApplication.kt`

```kotlin
@HiltAndroidApp
class SangeetMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

- Annotated with `@HiltAndroidApp` for Hilt DI
- Initializes Timber for debug logging

### 2. Main Activity

**File**: `app/src/main/kotlin/com/sangeetmind/app/MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SangeetMindTheme {
                Surface {
                    SangeetMindNavHost()
                }
            }
        }
    }
}
```

- Single Activity architecture with Compose
- Applies Material3 theme with day/night support

### 3. Raag Library Feature (Example)

**ViewModel**: `features/raaglibrary/RaagListViewModel.kt`

- Exposes `StateFlow<RaagListUiState>` with raags, filtered raags, loading, error
- Implements search filtering by name, Hindi name, description, tags
- Handles favorite toggle

**Screen**: `features/raaglibrary/ui/RaagListScreen.kt`

- Compose UI with TopAppBar, SearchBar, LazyColumn
- Displays raag cards with artwork, name, description, tags, favorite/play buttons
- Error and empty states

**Repository**: `features/raaglibrary/RaagRepository.kt`

- Fetches raags from MockApiService
- Caches in Room database
- Provides Flow-based API

### 4. ExoPlayer Service

**File**: `core/audio/PlayerService.kt`

- Extends `MediaSessionService` for background playback
- Creates notification channel for media controls
- Integrates with Media3 session

**File**: `core/audio/PlayerRepository.kt`

- Wraps ExoPlayer instance
- Exposes playback state via StateFlow
- Methods: `playRaag()`, `play()`, `pause()`, `seekTo()`, etc.

### 5. Network Layer

**File**: `core/network/ApiClient.kt`

- Retrofit + OkHttp + Moshi setup
- Auth interceptor for Bearer token
- Logging interceptor (debug only)
- Token refresh logic placeholder

**File**: `core/network/ApiService.kt`

- Defines API endpoints: auth, raag, meditation, astrology, user

### 6. Database Layer

**File**: `core/database/SangeetMindDatabase.kt`

- Room database with `RaagEntity`
- Type converters for enums and lists

**File**: `core/database/dao/RaagDao.kt`

- CRUD operations for raags
- Flow-based queries for reactive UI
- Favorite and download status updates

## Build Configuration

### Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKapt) apply false
    alias(libs.plugins.hilt) apply false
}
```

### App `build.gradle.kts`

- **Namespace**: `com.sangeetmind.app`
- **Compile SDK**: 34
- **Min SDK**: 24
- **Target SDK**: 34
- **Build Types**:
  - **Debug**: `applicationIdSuffix = ".debug"`, debuggable
  - **Release**: Minify + shrink resources, ProGuard rules, Play Store display name override
- **Dependencies**: All core + feature modules, Compose, Hilt, Timber

### Centralized Versions (`gradle/libs.versions.toml`)

- Kotlin 1.9.21
- Gradle 8.1.2
- Compose BOM 2023.10.01
- Hilt 2.48
- Room 2.6.1
- Retrofit 2.9.0
- Media3 1.1.1

## Tests

### Unit Tests

**File**: `features/raaglibrary/src/test/.../RaagListViewModelTest.kt`

- Tests search filtering by name, Hindi name, tags
- Tests clear search
- Tests loading and error states
- Uses Mockito + Coroutines Test

### Compose UI Tests

**File**: `app/src/androidTest/.../HomeScreenTest.kt`

- Asserts TopAppBar displays "Raag Library"
- Tests search bar interaction

## CI/CD

**File**: `.github/workflows/ci.yml`

- Runs on push/PR to `main` or `develop`
- Steps:
  1. Checkout code
  2. Set up JDK 17
  3. Cache Gradle
  4. Run lint
  5. Run unit tests
  6. Build debug APK
  7. Upload artifacts

## Localization

- **English**: `app/src/main/res/values/strings.xml`
- **Hindi**: `app/src/main/res/values-hi/strings.xml`
- Covers all UI strings: home, raag library, player, meditation, astrology, settings, auth, onboarding

## Accessibility

- Content descriptions for all interactive elements
- Dynamic font scaling support
- High-contrast theme support
- Voice search entry point (placeholder)

## Play Store Display Name Override

Set in `gradle.properties`:

```properties
PLAY_STORE_DISPLAY_NAME=Lumina Sangeet
```

Used in release builds via `manifestPlaceholders`.

## Running the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 34

### Build Commands

```bash
# Clean
./gradlew clean

# Build debug
./gradlew :app:assembleDebug

# Run tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

### Install on Device

```bash
./gradlew :app:installDebug
```

## Next Steps (TODOs in Code)

1. **Complete feature modules**: Implement player, meditation, astrology, settings screens
2. **Real backend integration**: Replace MockApiService with actual API calls
3. **Token refresh**: Implement automatic token refresh in auth interceptor
4. **Voice search**: Integrate Android SpeechRecognizer
5. **Google Sign-In**: Add OAuth authentication
6. **Remote config**: Add feature flags (Firebase Remote Config or custom)
7. **Analytics**: Integrate Firebase Analytics or custom solution
8. **Download manager**: Implement audio download and offline playback
9. **Widget**: Add home screen widget for quick playback
10. **Play Store**: Prepare release build and publish

## Commit Strategy

The project was scaffolded with incremental commits:

1. `chore: initialize multi-module project and Gradle config`
2. `feat(core): add network module and Retrofit client`
3. `feat(audio): add ExoPlayer service and cache config`
4. `feat(ui): scaffold Compose theme and common components`
5. `feat(raag): add raag library feature skeleton`
6. `test: add basic ViewModel unit tests and Compose test`
7. `ci: add GitHub Actions workflow`
8. `docs: add README and Play Store name instructions`

## Architecture Highlights

- **Clean Architecture**: Separation of concerns with domain, data, and presentation layers
- **MVVM**: ViewModels expose UI state via StateFlow
- **Hilt DI**: Compile-time dependency injection
- **Single Activity**: Navigation Compose for screen transitions
- **Reactive UI**: Flow-based data streams
- **Offline-first**: Room database caching with network fallback
- **Material3**: Modern design with dynamic theming

## Security Considerations

- Secrets stored in `local.properties` (gitignored)
- ProGuard rules for release builds
- EncryptedSharedPreferences for sensitive data (TODO)
- HTTPS-only network calls
- No hardcoded API keys

## Conclusion

This scaffold provides a **production-ready foundation** for the SangeetMind Android app. All core infrastructure is in place:

- ✅ Multi-module Gradle project
- ✅ Hilt DI wired end-to-end
- ✅ Compose UI with Material3 theme
- ✅ ExoPlayer background playback service
- ✅ Room database with caching
- ✅ Retrofit network layer
- ✅ Mock API for local development
- ✅ Unit and UI tests
- ✅ GitHub Actions CI
- ✅ Comprehensive documentation

The project is ready for feature development and backend integration. Follow the TODOs in the code and CONTRIBUTING.md for next steps.

---

**Generated**: 2025-11-15  
**Version**: 1.0.0

