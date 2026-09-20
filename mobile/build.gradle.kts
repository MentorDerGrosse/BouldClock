plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Die Handy-App.
 *
 * Wichtig: **dieselbe applicationId wie die Uhr.** Die Wearable Data Layer API
 * verbindet nur Apps mit gleichem Paketnamen und gleicher Signatur - mit einer
 * eigenen ID kaeme nie etwas an. Zwei APKs, zwei Geraete, ein Paketname.
 */
android {
    namespace = "at.mentor.bouldclockapp.mobile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "at.mentor.bouldclockapp"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.play.services.wearable)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.ui.tooling)
}
