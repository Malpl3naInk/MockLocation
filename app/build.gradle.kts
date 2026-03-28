plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.license.report)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
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

        // Reduce native library size
        externalNativeBuild {
            cmake {
                cppFlags("-O3 -fvisibility=hidden -fvisibility-inlines-hidden")
            }
        }
    }

    androidResources {
        // Remove unused language resources
        localeFilters.addAll(listOf("zh-rCN", "en"))
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        outputs.all {
            val abi = (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).filters
                .find { it.filterType == "ABI" }?.identifier ?: "universal"
            val name = "MockLocation-build${gitCommitCount()}-${gitCommitHash()}-${abi}.apk"
            this.outputFileName = name
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Material icons extended
    implementation(libs.androidx.compose.material.icons.extended)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.savedstate.ktx)
    implementation(libs.androidx.documentfile)
    ksp(libs.androidx.room.compiler)

    // Gson for JSON serialization
    implementation(libs.google.gson)

    // Google Open Location Code
    implementation(libs.google.openlocationcode)

    // LifecycleService
    implementation(libs.androidx.lifecycle.service)

    // Android Native Library
    implementation(project(":native"))

    // Mapbox map service
    implementation(libs.mapbox.ndk27)
    implementation(libs.mapbox.compose.ndk27)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // implementation(libs.firebase.crashlytics)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}