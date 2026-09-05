import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.sangeetmind.core.network"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Chart/LLM endpoints expect an x-api-key header (see astrologyAPI.ts on the
        // website). Set SANGEETMIND_CHART_API_KEY in local.properties, never commit it.
        val chartApiKey = localProperties.getProperty("SANGEETMIND_CHART_API_KEY") ?: ""
        buildConfigField("String", "CHART_API_KEY", "\"$chartApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":libs:models"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.org.jetbrains.kotlin.coroutines)
    
    // Retrofit & OkHttp
    implementation(libs.bundles.retrofit)
    implementation(libs.com.squareup.moshi)

    // Firebase Auth (ID token for the Authorization interceptor). Uses the base
    // firebase-auth artifact, not -ktx — Firebase merged the KTX extensions into
    // the base artifacts and firebase-auth-ktx is no longer part of the current BOM.
    api(platform(libs.firebase.bom))
    api(libs.firebase.auth)

    // Hilt
    implementation(libs.com.google.dagger.hilt)
    kapt(libs.com.google.dagger.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
}

