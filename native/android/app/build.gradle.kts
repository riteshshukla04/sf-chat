plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Crash reporting is optional: Sentry only starts when a DSN is passed at build time,
// same env vars as the React Native app (EXPO_PUBLIC_SENTRY_DSN, falling back to SENTRY_DSN).
val sentryDsn: String = System.getenv("EXPO_PUBLIC_SENTRY_DSN")?.takeIf { it.isNotBlank() }
    ?: System.getenv("SENTRY_DSN")?.takeIf { it.isNotBlank() }
    ?: ""

android {
    namespace = "dev.softwarefactory.chatter"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.softwarefactory.chatter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0" // keep in sync with app.config.ts
        buildConfigField("String", "SENTRY_DSN", "\"${sentryDsn.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug") // like the RN app: debug keystore, no store signing here
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.sentry.android)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
