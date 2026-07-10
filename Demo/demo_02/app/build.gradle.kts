plugins {
    // 注意：本机 AGP 9.2.1 会自动注入并应用 Kotlin 插件，
    // 因此【不要】再显式声明 id("org.jetbrains.kotlin.android")，
    // 否则会因重复应用报 "extension 'kotlin' already registered"。
    // 直接对齐 demo_01 的写法即可让 .kt 正常编译。
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.wb.example.demo_02"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.wb.example.demo_02"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.coroutines)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
