plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.android.application") version "8.6.0" apply false

    // 🌟 THE FIX: Modernize Kotlin and KSP
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false

    // 🌟 ADDED: The new way to handle Compose (Kotlin 2.0+)
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}