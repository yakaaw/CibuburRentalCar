plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.yaka.cibuburrentalcar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yaka.cibuburrentalcar"
        minSdk = 24  // Android 7.0 (Nougat) - Support 95%+ devices
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Splash Screen API (to control system splash)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Date Picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    // Networking dependencies removed for offline mode

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")



    // Date and Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Firebase BOM - manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase services
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase UI (optional - for easy auth UI)
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}