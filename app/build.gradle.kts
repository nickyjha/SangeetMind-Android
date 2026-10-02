import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

// Release signing: keystore.properties (gitignored) next to settings.gradle.kts holds the
// upload key's path and passwords. Without it, release builds are unsigned (CI / fresh clone).
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.sangeetmind.app"
    compileSdk = 36

    signingConfigs {
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.sangeetmind.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            // No applicationIdSuffix: the registered Firebase Android app (and its
            // google-services.json) is "com.sangeetmind.app" only, with no separate
            // ".debug" entry. Keeping the suffix would make Firebase Auth unusable
            // in debug builds. Re-add a suffix (and register a second Firebase app
            // for it) if side-by-side debug/release installs are ever needed.
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use Play Store display name from gradle.properties
            val playStoreDisplayName = project.findProperty("PLAY_STORE_DISPLAY_NAME") as String? ?: "AstroGeet"
            manifestPlaceholders["appLabel"] = playStoreDisplayName
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:audio"))

    // Feature modules
    implementation(project(":features:onboarding"))
    implementation(project(":features:auth"))
    implementation(project(":features:raaglibrary"))
    implementation(project(":features:player"))
    implementation(project(":features:meditation"))
    implementation(project(":features:astrology"))
    implementation(project(":features:settings"))

    // Libs
    implementation(project(":libs:models"))
    implementation(project(":integration:backend-stub"))

    // Firebase BOM must be declared in every module whose own dependency
    // resolution touches a Firebase library — an upstream module's `api platform(...)`
    // does not propagate the BOM's version constraints across project boundaries.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    // MainActivity injects PlayBillingManager, whose supertype lives in the billing library.
    implementation(libs.android.billing)

    // MainActivity implements Razorpay's PaymentResultWithDataListener directly.
    implementation(libs.razorpay.checkout)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.com.google.dagger.hilt)
    kapt(libs.com.google.dagger.hilt.compiler)

    // Coroutines
    implementation(libs.org.jetbrains.kotlin.coroutines)

    // Timber
    implementation(libs.com.jakewharton.timber)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

