plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

/**
 * Gemeinsamer Kern von Uhr und Handy.
 *
 * Enthaelt Modell, Metriken, Zustandsmaschine, Datenbank und das Dateiformat der
 * Sensordaten. Die Paketnamen sind dieselben wie zuvor im Uhr-Modul - deshalb
 * musste beim Verschieben kein einziger Import angefasst werden.
 */
android {
    namespace = "at.mentor.bouldclockapp.shared"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    // Exportiertes Schema = Grundlage fuer Migrationen und Migrationstests.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    // api statt implementation: das Uhr-Modul arbeitet direkt mit Room-Typen
    // und Coroutines-Flows aus diesem Modul.
    api(libs.room.runtime)
    api(libs.room.ktx)
    api(libs.kotlinx.coroutines.android)
    api(libs.datastore.preferences)
    api(libs.play.services.wearable)
    api(libs.kotlinx.coroutines.play.services)
    ksp(libs.room.compiler)

    // org.json steckt in android.jar und liefert im JVM-Test nur Attrappen -
    // diese echte Umsetzung macht die Serialisierung ohne Geraet pruefbar.
    testImplementation(libs.org.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
