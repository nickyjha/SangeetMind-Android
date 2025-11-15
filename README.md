# SangeetMind Android

[![Android CI](https://github.com/yourusername/sangeetmind-android/workflows/Android%20CI/badge.svg)](https://github.com/yourusername/sangeetmind-android/actions)

A mobile-first Android app for raag-based meditation and music with astrology personalization. Built with Kotlin, Jetpack Compose, and modern Android architecture.

## Features

- 🎵 **Raag Library**: Browse and search classical Indian raags with filtering by time, mood, and intensity
- 🧘 **Meditation Sessions**: Guided meditation with ambient raag-based soundscapes
- ⭐ **Astrology Integration**: Personalized recommendations based on birth chart
- 🎧 **Audio Player**: Background playback with ExoPlayer, offline caching, and media controls
- 🌐 **Localization**: Full support for English and Hindi (हिंदी)
- ♿ **Accessibility**: Voice search, content descriptions, and dynamic font scaling
- 🌙 **Dark Mode**: Material3 theming with automatic day/night support

## Architecture

This project follows **Clean Architecture** principles with a multi-module Gradle setup:

```
sangeetmind-android/
├── app/                          # Main application module
├── core/
│   ├── common/                   # Common utilities, Result wrapper, Constants
│   ├── ui/                       # Shared Compose components, theme, typography
│   ├── network/                  # Retrofit, OkHttp, API client
│   ├── database/                 # Room database, DAOs
│   └── audio/                    # ExoPlayer service, audio caching
├── features/
│   ├── onboarding/               # Onboarding flows
│   ├── auth/                     # Login/signup, token management
│   ├── raaglibrary/              # Raag browsing, search, filters
│   ├── player/                   # Audio player UI and controls
│   ├── meditation/               # Meditation sessions
│   ├── astrology/                # Birth chart input and recommendations
│   └── settings/                 # User preferences, language toggle
├── libs/
│   └── models/                   # Domain models (Raag, User, etc.)
└── integration/
    └── backend-stub/             # Mock API for local development
```

### Tech Stack

- **Language**: Kotlin 1.9.21
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt
- **Async**: Kotlin Coroutines + Flow
- **Networking**: Retrofit + OkHttp + Moshi
- **Database**: Room + DataStore
- **Audio**: ExoPlayer (Media3)
- **Navigation**: Navigation Compose
- **Testing**: JUnit, Mockito, Compose UI Test

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34
- Gradle 8.3+ (included via wrapper)

### Clone and Build

```bash
git clone https://github.com/yourusername/sangeetmind-android.git
cd sangeetmind-android
./gradlew build
```

### Run the App

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Configuration

### Play Store Display Name

By default, the app is named **SangeetMind**. To override the display name for Play Store release builds:

1. Open `gradle.properties` or create `local.properties`
2. Set the property:
   ```properties
   PLAY_STORE_DISPLAY_NAME=Lumina Sangeet
   ```
3. Build the release variant:
   ```bash
   ./gradlew :app:assembleRelease
   ```

The release APK will use the custom display name.

### API Configuration

The app currently uses a **mock backend** (`integration:backend-stub`) for local development. To connect to a real backend:

1. Update `BASE_URL` in `core/common/src/main/kotlin/.../Constants.kt`
2. Implement real API endpoints in `core/network/ApiService.kt`
3. Remove or disable `MockApiService` in feature repositories

### Signing Configuration

For release builds, configure signing in `app/build.gradle.kts` or use a `keystore.properties` file:

```properties
# keystore.properties
storeFile=/path/to/keystore.jks
storePassword=yourStorePassword
keyAlias=yourKeyAlias
keyPassword=yourKeyPassword
```

Reference it in your build script:

```kotlin
signingConfigs {
    create("release") {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}
```

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

### Test Coverage

Unit tests are provided for:
- `RaagListViewModel` (search filtering logic)
- Compose UI test for Home screen (top app bar assertion)

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Runs lint checks
2. Executes unit tests
3. Builds debug APK
4. Uploads artifacts

To enable CI:
1. Push your code to GitHub
2. The workflow runs automatically on push/PR to `main` or `develop` branches

## Project Structure Details

### Modules

| Module | Purpose |
|--------|---------|
| `:app` | Application entry point, navigation, MainActivity |
| `:core:common` | Shared utilities, Result wrapper, dispatchers |
| `:core:ui` | Compose theme, typography, shared components |
| `:core:network` | Retrofit setup, auth interceptor, token manager |
| `:core:database` | Room DB, DAOs, entities |
| `:core:audio` | ExoPlayer service, player repository, caching |
| `:features:*` | Feature modules with UI, ViewModel, Repository |
| `:libs:models` | Domain models (Raag, User, AuthToken, etc.) |
| `:integration:backend-stub` | Mock API service for development |

### Key Files

- `SangeetMindApplication.kt`: Hilt app initialization, Timber setup
- `MainActivity.kt`: Compose entry point
- `SangeetMindNavHost.kt`: Navigation graph
- `PlayerService.kt`: Foreground service for audio playback
- `RaagListScreen.kt`: Example Compose UI with search and filtering

## Roadmap

- [ ] Complete all feature modules (player, meditation, astrology, settings)
- [ ] Implement real backend API integration
- [ ] Add Google Sign-In for authentication
- [ ] Implement voice search with SpeechRecognizer
- [ ] Add remote config for feature flags
- [ ] Integrate analytics (Firebase or custom)
- [ ] Implement audio download and offline playback
- [ ] Add widget for quick playback controls
- [ ] Publish to Google Play Store

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## Security

See [SECURITY.md](SECURITY.md) for information on reporting security vulnerabilities.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please open an issue on GitHub or contact the maintainers.

---

**Note**: This is a scaffold project. Backend endpoints, authentication, and some features are stubbed for demonstration purposes. Integrate with your actual backend and complete the TODOs in the code before production use.

