import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

configure<ApplicationExtension> {
    namespace = "nep.timeline.cirno"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        targetSdk = 36
        versionCode = 8
        versionName = "8.0"

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
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.commons.io)
    compileOnly("de.robv.android.xposed:api:82:sources")
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.chrisbanes.haze)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val libsuVersion = "6.0.0"
    implementation("com.github.topjohnwu.libsu:core:$libsuVersion")
    implementation("com.github.topjohnwu.libsu:service:$libsuVersion")
    implementation("com.github.topjohnwu.libsu:io:$libsuVersion")
}