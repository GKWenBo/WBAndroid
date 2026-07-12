plugins {
    alias(libs.plugins.android.application)
    // Compose 编译器插件（Kotlin 2.x 用它开启 @Composable 编译支持）。
    // 坑①提醒：不要再加 org.jetbrains.kotlin.android —— AGP 9.2.1 会自动应用，
    // 重复声明报 extension 'kotlin' already registered。
    alias(libs.plugins.kotlin.compose)
}

android {
    // namespace：R 类与 BuildConfig 的包名（AGP 8 起与 applicationId 解耦）
    namespace = "com.wb.wanreader"
    // compileSdk：编译用的 SDK API 面（能"看见"哪些 API）。iOS 对照：≈ Base SDK
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // applicationId：应用在设备/商店的唯一身份。iOS 对照：Bundle Identifier
        applicationId = "com.wb.wanreader"
        // minSdk：最低支持系统版本。iOS 对照：Deployment Target
        minSdk = 24
        // targetSdk：声明"已适配到"的版本，影响系统兼容行为，商店有硬性下限要求
        targetSdk = 36
        versionCode = 1        // 内部递增版本号。iOS 对照：CFBundleVersion
        versionName = "0.1.0"  // 用户可见版本。iOS 对照：CFBundleShortVersionString

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // S0 先关闭混淆/优化，S9（构建变体与多渠道）再系统性开启并讲 R8
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
    // XML 主题 Theme.Material3.* 的资源提供方（AndroidManifest 引用的主题定义在它里面）
    implementation(libs.material)

    // Compose：BOM 统一版本，以下各库不写版本号
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)
}
