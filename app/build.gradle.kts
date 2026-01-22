plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.license.report)
    kotlin("kapt")
}

licenseReport {
    // Generate reports
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = true
    generateTextReport = false

    // Copy reports - These options are ignored for Java projects
    copyCsvReportToAssets = false
    copyHtmlReportToAssets = false
    copyJsonReportToAssets = true
    copyTextReportToAssets = false
    useVariantSpecificAssetDirs = false

    // Show versions in the report - default is false
    showVersions = true
}

// ===== Version Build =====
fun gitCommitHash(): String {
    return try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
        output
    } catch (_: Exception) {
        "unknown"
    }
}

fun gitCommitCount(): Int {
    return try {
        val output = providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText.get().trim()
        output.toInt()
    } catch (_: Exception) {
        0
    }
}

// ===== Version Base =====
val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

android {
    namespace = "ink.moling.mocklocation"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "ink.moling.mocklocation"
        minSdk = 26
        targetSdk = 36
        versionCode = gitCommitCount()
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
                ?: findProperty("KEYSTORE_FILE")?.let { file(it.toString()) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: findProperty("KEYSTORE_PASSWORD")?.toString()
            keyAlias = System.getenv("KEY_ALIAS")
                ?: findProperty("KEY_ALIAS")?.toString()
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: findProperty("KEY_PASSWORD")?.toString()
        }
    }

    buildTypes {
        val isAction = project.hasProperty("ACTION")
        val versionSuffix = findProperty("VERSION_SUFFIX")?.toString() ?: "alpha"

        val versionPre = if (isAction) "-$versionSuffix" else ""
        val versionBuild    = if (isAction) {
            "+git.${gitCommitHash()}"
        } else {
            "+local.${gitCommitCount()}"
        }
        debug {
            versionNameSuffix = versionPre + versionBuild
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            if (isAction) {
                versionNameSuffix = versionPre + versionBuild
            }
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.savedstate.ktx)
    kapt(libs.androidx.room.compiler)

    // Gson for JSON serialization
    implementation(libs.google.gson)

    // Google Open Location Code
    implementation(libs.google.openlocationcode)

    // LifecycleService
    implementation(libs.androidx.lifecycle.service)

    // Android Native Library
    implementation(project(":native"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}