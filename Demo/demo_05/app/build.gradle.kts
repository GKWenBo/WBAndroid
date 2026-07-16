plugins {
    alias(libs.plugins.android.application)
    // Compose 编译器插件（Kotlin 2.x 用这个开启 @Composable 支持）。
    // 注意：不要再加 org.jetbrains.kotlin.android —— 它会被 AGP 自动应用，
    // kotlin.compose 会随之内含 Kotlin Android 支持，重复声明会报 extension 冲突。
    alias(libs.plugins.kotlin.compose)
    // kotlinx.serialization 序列化插件（Retrofit 用 JSON 解析实体）
    alias(libs.plugins.kotlin.serialization)
    // KSP 注解处理器（Room 的 @Entity/@Dao 代码生成必需）
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wb.example.demo_05"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.wb.example.demo_05"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Compose：BOM 统一版本，下面各库都不写版本号
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // —— W4 数据层（与 W3 相同技术栈）——
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
