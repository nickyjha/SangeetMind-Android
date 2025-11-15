plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sangeetmind.integration.backendstub"
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
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":libs:models"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.org.jetbrains.kotlin.coroutines)
    
    // Retrofit & OkHttp
    implementation(libs.bundles.retrofit)
    implementation(libs.com.squareup.moshi)
    
    // Hilt
    implementation(libs.com.google.dagger.hilt)
    kapt(libs.com.google.dagger.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
}

