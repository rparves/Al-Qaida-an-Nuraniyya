plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.nooraniqaida.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nooraniqaida.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Size: keep only English + Bangla resources from all libraries.
        resourceConfigurations += listOf("en", "bn", "ar")
    }

    buildTypes {
        release {
            // Size: R8 code shrinking + unused resource removal.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // For local testing only. Replace with your own signing config before publishing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    androidResources {
        // Optional bundled Opus audio is played straight from the APK, so keep it uncompressed.
        noCompress += "ogg"
    }

    bundle {
        // The in-app language switch needs both languages on every device.
        language { enableSplit = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        // Size: store the ML Kit native library compressed (roughly halves the download).
        jniLibs { useLegacyPackaging = true }
    }

    // Size: one APK per phone CPU type instead of a universal APK with all four.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text-google-fonts") // fonts download at runtime, 0 MB in the APK
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.appcompat:appcompat:1.7.0") // per-app language (English/Bangla) on Android 7+

    // On-device handwriting recognition. The Arabic model (~20 MB) is downloaded on first use, not bundled.
    implementation("com.google.mlkit:digital-ink-recognition:18.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
