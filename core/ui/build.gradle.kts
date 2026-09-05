plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.sangeetmind.core.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    
    // Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    implementation(libs.androidx.compose.ui.tooling)
    // Full icon set (Icons.Default.X beyond the small core-bundled subset). `api` so
    // every feature module depending on core:ui gets it without repeating the BOM.
    api(libs.androidx.compose.material.icons.extended)

    // Testing
    testImplementation(libs.junit)
}

