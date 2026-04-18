plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.license.report)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
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

// 支持从命令行参数覆盖版本名 (用于手动触发构建)
// 例如: ./gradlew assembleRelease -PVERSION_NAME=2.0.0
val customVersionName = findProperty("VERSION_NAME")?.toString()
val baseVersionName = customVersionName ?: "$versionMajor.$versionMinor.$versionPatch"

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
        versionName = baseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Reduce native library size
        externalNativeBuild {
            cmake {
                cppFlags("-O3 -fvisibility=hidden -fvisibility-inlines-hidden")
            }
        }
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
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
        val isDispatch = findProperty("VERSION_SUFFIX")?.toString() == ""

        // 版本后缀规则:
        // - GitHub Actions 自动构建 (push/PR): beta + git hash
        // - 本地构建: alpha + local commit count
        // - 手动触发构建 (workflow_dispatch): 纯净无后缀
        val versionPre = when {
            isDispatch -> ""                           // 手动触发: 无后缀
            isAction -> "-beta"                        // GitHub Actions: beta
            else -> "-alpha"                           // 本地构建: alpha
        }
        val versionBuild = when {
            isDispatch -> ""                           // 手动触发: 无 build 信息
            isAction -> "+git.${gitCommitHash()}"      // GitHub Actions: git hash
            else -> "+local.${gitCommitCount()}"       // 本地构建: local commit count
        }

        val versionSuffix = versionPre + versionBuild

        debug {
            versionNameSuffix = versionSuffix
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            versionNameSuffix = versionSuffix
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
            // 手动触发构建时使用版本名，其他情况使用 git hash
            val versionSuffix = customVersionName?.let { "rel$it" } ?: gitCommitHash()
            val name = "MockLocation-build${gitCommitCount()}-${versionSuffix}.apk"
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = name
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

    lint {
        // CI 优化：禁用发布构建的 lint 检查以加速构建
        // 本地开发时仍可通过 ./gradlew lint 手动运行
        checkReleaseBuilds = false
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = false
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

    // Mapbox map service
    implementation(libs.mapbox.ndk27)
    implementation(libs.mapbox.compose.ndk27)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // YukiHookAPI 基础依赖
    implementation(libs.yukihookapi.api)
    // 推荐使用 KavaRef 作为核心反射 API
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    compileOnly(libs.xposed.api)
    // 作为 Xposed 模块使用务必添加，其它情况可选
    ksp(libs.yukihookapi.ksp.xposed)
}