# Android 工程配置要点(iOS开发者视角)

> 对比基准:Xcode项目配置 ↔ Android Studio/Gradle配置

---

## 1. 项目结构对比

| iOS (Xcode) | Android (Gradle) | 说明 |
|---|---|---|
| `.xcodeproj` / `.xcworkspace` | `settings.gradle.kts` | 工程/工作区定义 |
| Target | Module | 一个App可包含多个module(类似多target) |
| `Info.plist` | `AndroidManifest.xml` | 应用元信息、权限声明 |
| `Podfile` / `Package.swift` | `build.gradle.kts` (module级) | 依赖管理 |
| `.xcconfig` | `gradle.properties` | 构建环境变量 |
| Scheme | Build Variant | 编译配置组合 |

**典型目录结构:**
```
项目根目录/
├── settings.gradle.kts        # 声明包含哪些module
├── build.gradle.kts            # 根级构建脚本(插件版本声明)
├── gradle.properties           # 全局属性(JVM参数、AndroidX开关等)
├── gradle/
│   ├── wrapper/                 # Gradle Wrapper(锁定Gradle版本,类似.xcode-version)
│   └── libs.versions.toml       # 版本目录(Version Catalog,统一管理依赖版本)
└── app/                         # 主module,类似主Target
    ├── build.gradle.kts         # module级构建配置
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java(或kotlin)/
        │   └── res/              # 资源(类似Assets.xcassets + Localizable.strings)
        ├── debug/                # Debug变体专属资源/代码
        └── release/               # Release变体专属
```

---

## 2. 核心配置文件详解

### 2.1 `settings.gradle.kts` —— 声明模块 + 仓库源

这是Gradle特有的概念,iOS没有直接对应物(最接近Workspace的scheme管理)。

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/") } // 插件仓库,腾讯云镜像
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 强制统一仓库源,禁止module各自声明
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") } // 阿里云镜像,国内加速
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"
include(":app")
include(":core:network")   // 多module示例,类似iOS的多Framework Target
include(":feature:login")
```

> ⚠️ 你已验证过的配置:**发行版下载(Gradle Distribution)走腾讯云,依赖解析(Maven仓库)走阿里云**——两者是不同层面,不能混用镜像地址。

### 2.2 `gradle/wrapper/gradle-wrapper.properties` —— 锁定Gradle版本

类似锁定Xcode版本,保证团队构建一致性:

```properties
distributionUrl=https\://mirrors.tencent.com/gradle/gradle-8.9-bin.zip
```

### 2.3 `build.gradle.kts`(module级)—— 相当于Target的Build Settings

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Compose编译器插件(K2时代需要)
}

android {
    namespace = "com.wenbo.myapp"     // 相当于Bundle Identifier
    compileSdk = 35                    // 编译用SDK版本,类似"Base SDK"

    defaultConfig {
        applicationId = "com.wenbo.myapp"  // 真正的Bundle ID,发布用
        minSdk = 24                         // 类似iOS Deployment Target
        targetSdk = 35
        versionCode = 1                     // 类似CFBundleVersion(纯数字,递增)
        versionName = "1.0.0"               // 类似CFBundleShortVersionString
    }

    buildTypes {
        release {
            isMinifyEnabled = true          // 开启R8混淆压缩,类似开启编译优化+去符号
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"  // Debug包与Release包可共存安装
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))  // BOM统一版本,类似CocoaPods的:podspec依赖树
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
}
```

### 2.4 `libs.versions.toml` —— Version Catalog(强烈推荐)

类似Swift Package Manager的`Package.resolved`,但更像是**集中式版本管理表**,避免各module依赖版本打架:

```toml
[versions]
kotlin = "2.1.0"
composeBom = "2025.01.00"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.15.0" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }

[plugins]
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

---

## 3. 权限与清单配置(对应 Info.plist)

`AndroidManifest.xml` 是Android独有的核心配置文件,声明组件、权限、四大组件入口:

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- 类似iOS的NSAppTransportSecurity网络权限声明,但Android需显式列出每个权限 -->

    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:theme="@style/Theme.MyApp">

        <activity
            android:name=".MainActivity"
            android:exported="true">   <!-- 关键:决定其他App能否启动该Activity,类似URL Scheme白名单思路 -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

> ⚠️ **易错点**:Android 12+ 要求所有声明了`intent-filter`的组件必须**显式**写`android:exported`属性,否则编译报错——这比iOS的Info.plist权限声明更严格。

---

## 4. 国内网络加速配置汇总(你已验证的组合)

| 用途 | 推荐源 | 配置位置 |
|---|---|---|
| Gradle发行版下载 | 腾讯云镜像 | `gradle-wrapper.properties` |
| Maven依赖解析 | 阿里云镜像 | `settings.gradle.kts` |
| SDK Platform/Build-tools | Android Studio内置SDK Manager(可配代理) | Studio设置 |
| System Image(模拟器镜像) | **不走以上两个镜像**,用`aria2c`并行下载官方源,或androiddevtools.cn | 命令行/浏览器 |

---

## 5. Build Variant(构建变体)—— 对应 Xcode Scheme + Configuration

Android的变体 = **Build Type × Product Flavor** 的笛卡尔积,比iOS的Scheme机制更结构化:

```kotlin
android {
    buildTypes { debug {}; release {} }
    productFlavors {
        create("dev")  { applicationIdSuffix = ".dev" }
        create("prod") { }
    }
}
// 生成变体: devDebug, devRelease, prodDebug, prodRelease
```

类似iOS里用不同`.xcconfig` + Scheme组合出Dev/Staging/Prod,但Gradle把这个组合关系变成了**声明式配置**而非手动拼Scheme。

---

## 6. 自查清单

- [ ] `settings.gradle.kts`中Gradle插件仓库与依赖仓库是否都配置了国内镜像?
- [ ] `gradle-wrapper.properties`的`distributionUrl`是否指向腾讯云?
- [ ] 是否使用`libs.versions.toml`统一管理版本,而非在各module里散落写版本号?
- [ ] `minSdk`选择是否评估过目标用户设备分布(国内建议不低于24)?
- [ ] Release构建是否开启`isMinifyEnabled` + 配置好`proguard-rules.pro`?
- [ ] Manifest中所有`exported`组件是否经过安全评估(避免被其他App任意启动)?

---

*下一步建议:结合Jetpack Compose学习时,重点关注`buildFeatures { compose = true }`与Compose Compiler插件版本的匹配关系(Kotlin 2.0+后已改为独立插件`kotlin-compose`)。*
