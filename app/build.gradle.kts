plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    namespace = "com.overlay.android.sample"
    compileSdk = 34

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.overlay.android.sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("aibui.jks") // Replace with your keystore path
            storePassword = "n7z%hujspS7J>[F!" // Replace with your store password
            keyAlias = "key0" // Replace with your key alias
            keyPassword = "n7z%hujspS7J>[F!" // Replace with your key password
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(files("libs/aibuysdk-release.aar"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat.ktx)
    implementation(libs.androidx.contraintlayout)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.exoplayer)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.gson)

    implementation(libs.stripe)
    implementation(libs.glide)
    implementation(libs.android.flowlayout.manager)
    kapt(libs.glide.kapt)

    implementation(libs.bundles.retrofit)
    implementation(libs.apollo.graphql)

    implementation(libs.android.svg)
    implementation(libs.android.material)
    implementation (libs.android.flowlayout.manager)
    implementation(libs.slidingpanel)
    implementation(libs.flexbox)
    implementation(libs.apollo.graphql)
    implementation(libs.circle.indicator)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okio)

    //   implementation(project(":shared"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)

}