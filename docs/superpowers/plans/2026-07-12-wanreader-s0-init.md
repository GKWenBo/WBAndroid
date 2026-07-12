# WanReader S0 · 工程初始化与配置 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在仓库根目录创建可真机运行的 `WanReader/` Android 工程（企业级配置起点），并交付 S0 教学文档与教学进度文件。

**Architecture:** 单 `app` 模块 Compose 工程；Gradle Kotlin DSL + Version Catalog；所有版本组合复用 `Demo/demo_03` 已在学员本机验证通过的配置（AGP 9.2.1 / Kotlin 2.3.20 / Gradle 9.4.1 / compileSdk 36）。S0 只交付"最小可跑模板 + 讲透配置"，分层目录与 Hilt 留给 S1。

**Tech Stack:** Kotlin 2.3.20、AGP 9.2.1、Gradle 9.4.1、Jetpack Compose（BOM 2025.06.00）、Material 3。

## Global Constraints

- 包名/namespace：`com.wb.wanreader`；rootProject name：`WanReader`
- 版本锁定：`agp = "9.2.1"`、`kotlin = "2.3.20"`、`composeBom = "2025.06.00"`、Gradle wrapper `9.4.1`、`compileSdk 36 (minorApiLevel 1)`、`minSdk 24`、`targetSdk 36`
- **模块 `plugins` 块禁止声明 `id("org.jetbrains.kotlin.android")`**（AGP 9.2.1 自动应用，重复声明报 `extension 'kotlin' already registered`）；Kotlin 版本只在 `settings.gradle.kts` 的 `pluginManagement.plugins` 声明
- 沙箱无 Android SDK：本计划内验证 = 文件结构比对 + TOML/XML 语法解析；编译/真机验收由学员本地 Android Studio 完成（Task 5 门禁）
- git 提交信息不加 Co-Authored-By 行
- 每节课必有配套教学文档：S0 交付 `WanReader/doc/S0_工程初始化与配置.md`
- 教学文档风格：中文、iOS 视角对照、含"坑与解决方案"小节、末尾验收 checklist

---

### Task 1: Gradle 构建骨架（根目录 7 件套）

**Files:**
- Create: `WanReader/settings.gradle.kts`
- Create: `WanReader/build.gradle.kts`
- Create: `WanReader/gradle.properties`
- Create: `WanReader/gradle/libs.versions.toml`
- Create: `WanReader/.gitignore`
- Copy: `WanReader/gradlew`、`WanReader/gradlew.bat`、`WanReader/gradle/wrapper/gradle-wrapper.jar`、`WanReader/gradle/wrapper/gradle-wrapper.properties`、`WanReader/gradle/gradle-daemon-jvm.properties`（均自 `Demo/demo_03` 原样复制）

**Interfaces:**
- Produces: Version Catalog 别名供 Task 2 引用——`libs.plugins.android.application`、`libs.plugins.kotlin.compose`、`libs.androidx.core.ktx`、`libs.material`、`libs.androidx.compose.bom`、`libs.androidx.ui`、`libs.androidx.ui.graphics`、`libs.androidx.ui.tooling`、`libs.androidx.ui.tooling.preview`、`libs.androidx.material3`、`libs.androidx.activity.compose`

- [ ] **Step 1: 复制 wrapper 与守护进程配置（demo_03 已验证的二进制与脚本，不手写）**

```bash
mkdir -p /Users/wenbo/Desktop/WBAndroid/WanReader/gradle/wrapper
cp /Users/wenbo/Desktop/WBAndroid/Demo/demo_03/gradlew /Users/wenbo/Desktop/WBAndroid/WanReader/gradlew
cp /Users/wenbo/Desktop/WBAndroid/Demo/demo_03/gradlew.bat /Users/wenbo/Desktop/WBAndroid/WanReader/gradlew.bat
cp /Users/wenbo/Desktop/WBAndroid/Demo/demo_03/gradle/wrapper/gradle-wrapper.jar /Users/wenbo/Desktop/WBAndroid/WanReader/gradle/wrapper/gradle-wrapper.jar
cp /Users/wenbo/Desktop/WBAndroid/Demo/demo_03/gradle/wrapper/gradle-wrapper.properties /Users/wenbo/Desktop/WBAndroid/WanReader/gradle/wrapper/gradle-wrapper.properties
cp /Users/wenbo/Desktop/WBAndroid/Demo/demo_03/gradle/gradle-daemon-jvm.properties /Users/wenbo/Desktop/WBAndroid/WanReader/gradle/gradle-daemon-jvm.properties
chmod +x /Users/wenbo/Desktop/WBAndroid/WanReader/gradlew
```

- [ ] **Step 2: 写 `WanReader/settings.gradle.kts`**

```kotlin
// settings.gradle.kts —— 工程的"户口本"：声明有哪些模块、插件和依赖从哪里下载。
// iOS 对照：≈ Xcode 的 project.pbxproj（模块清单）+ SPM 的 Package.resolved 来源配置。
pluginManagement {
    plugins {
        // Kotlin 版本的【单一真源】：只在这里写版本号，模块内引用插件一律不写版本。
        // 坑①：AGP 9.2.1 会自动应用 kotlin.android 插件，
        //       模块 plugins 块里【不要】再声明 id("org.jetbrains.kotlin.android")，
        //       否则报 extension 'kotlin' already registered（demo_02 踩过）。
        id("org.jetbrains.kotlin.android") version "2.3.20"
        // Compose 编译器插件（Kotlin 2.x 起随 Kotlin 同版本发布），必须显式声明。
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
    }
    repositories {
        google {
            content {
                // 只到 Google 仓库找这三类组件，加速解析（国内网络尤其明显）
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // 自动下载匹配的 JDK 工具链，避免"本机 JDK 版本不对"一类的坑
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // 企业规范：禁止模块私自声明仓库，依赖来源全部集中在这里管控
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WanReader"
include(":app")
```

- [ ] **Step 3: 写 `WanReader/build.gradle.kts`（根构建脚本）**

```kotlin
// 根 build.gradle.kts —— 只做一件事：把插件"登记"到类路径但不应用（apply false），
// 由各模块自行选用。iOS 对照：≈ Podfile 顶部的平台/源声明，不含具体 target 配置。
plugins {
    alias(libs.plugins.android.application) apply false
}
```

- [ ] **Step 4: 写 `WanReader/gradle.properties`**

```properties
# 全局构建开关（对所有模块生效）。iOS 对照：≈ xcconfig + Xcode Build Settings 的全局层。

# Gradle 守护进程 JVM 内存；工程变大或多模块后可上调
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# 配置缓存：脚本没变就跳过配置阶段，二次构建明显提速（AGP 9 已稳定支持）
org.gradle.configuration-cache=true

# Kotlin 官方代码风格
kotlin.code.style=official
```

- [ ] **Step 5: 写 `WanReader/gradle/libs.versions.toml`（Version Catalog）**

```toml
# Version Catalog —— 全工程依赖版本的单一真源。
# iOS 对照：≈ 把 Package.swift 里所有 .package(url:from:) 抽到一个集中清单。
# S0 只放模板 App 所需的最小集；后续阶段按需增量添加。

[versions]
agp = "9.2.1"
kotlin = "2.3.20"
composeBom = "2025.06.00"
coreKtx = "1.10.1"
material = "1.10.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
# Material Components：XML 主题 Theme.Material3.* 的资源提供方（Manifest 主题需要）
material = { group = "com.google.android.material", name = "material", version.ref = "material" }

# Compose：BOM 统一定版，下面各库一律不写版本号
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 6: 写 `WanReader/.gitignore`**

```gitignore
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

- [ ] **Step 7: 验证骨架**

```bash
cd /Users/wenbo/Desktop/WBAndroid
# TOML 可解析
python3 -c "import tomllib; tomllib.load(open('WanReader/gradle/libs.versions.toml','rb')); print('TOML OK')"
# wrapper 与 demo_03 完全一致（jar 为二进制，必须逐字节相同）
diff Demo/demo_03/gradle/wrapper/gradle-wrapper.properties WanReader/gradle/wrapper/gradle-wrapper.properties && \
cmp Demo/demo_03/gradle/wrapper/gradle-wrapper.jar WanReader/gradle/wrapper/gradle-wrapper.jar && echo "WRAPPER OK"
ls WanReader/gradlew WanReader/settings.gradle.kts WanReader/build.gradle.kts WanReader/gradle.properties WanReader/.gitignore
```

Expected: 输出 `TOML OK`、`WRAPPER OK`，ls 列出全部 5 个文件，无报错。

- [ ] **Step 8: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader
git commit -m "WanReader S0：Gradle 构建骨架（settings/根build/属性/版本目录/wrapper）"
```

---

### Task 2: app 模块（可运行的最小模板）

**Files:**
- Create: `WanReader/app/build.gradle.kts`
- Create: `WanReader/app/.gitignore`
- Create: `WanReader/app/src/main/AndroidManifest.xml`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/MainActivity.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Theme.kt`
- Create: `WanReader/app/src/main/res/values/strings.xml`、`res/values/themes.xml`、`res/xml/backup_rules.xml`、`res/xml/data_extraction_rules.xml`
- Copy: 启动图标资源（`res/drawable/ic_launcher_*.xml`、`res/mipmap-*/`）自 `Demo/demo_03` 原样复制

**Interfaces:**
- Consumes: Task 1 的 Version Catalog 别名（见 Task 1 Produces）
- Produces: `WanReaderTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)`（包 `com.wb.wanreader.ui.theme`，S1 的导航骨架将复用）；`MainActivity : ComponentActivity`（S1 将改造其 setContent 内容）

- [ ] **Step 1: 写 `WanReader/app/build.gradle.kts`**

```kotlin
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
```

- [ ] **Step 2: 写 `WanReader/app/.gitignore`**

```gitignore
/build
```

- [ ] **Step 3: 写 `WanReader/app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Manifest：App 的"身份证 + 权限清单 + 组件注册表"。iOS 对照：Info.plist（但组件必须显式注册） -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WanReader">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 4: 写 `WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Theme.kt`**

```kotlin
package com.wb.wanreader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// App 级主题包装。iOS 对照：≈ SwiftUI 根视图上统一注入的 environment 配色。
// S0 先用 Material3 默认配色跑通深浅色切换，S1 再定制品牌色。
@Composable
fun WanReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

- [ ] **Step 5: 写 `WanReader/app/src/main/java/com/wb/wanreader/MainActivity.kt`**

```kotlin
package com.wb.wanreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wb.wanreader.ui.theme.WanReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compose 入口：setContent 替代传统 setContentView(R.layout.xxx)
        setContent {
            WanReaderTheme {
                HelloScreen()
            }
        }
    }
}

@Composable
fun HelloScreen() {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WanReader 工程初始化成功 🎉",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelloScreenPreview() {
    WanReaderTheme {
        HelloScreen()
    }
}
```

- [ ] **Step 6: 写 res 基础资源**

`WanReader/app/src/main/res/values/strings.xml`：

```xml
<resources>
    <string name="app_name">WanReader</string>
</resources>
```

`WanReader/app/src/main/res/values/themes.xml`：

```xml
<resources>
    <!-- XML 主题只服务"Compose 接管前"的启动瞬间（窗口背景、状态栏）；页面内主题由 WanReaderTheme 负责 -->
    <style name="Base.Theme.WanReader" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Customize your light theme here. -->
    </style>

    <style name="Theme.WanReader" parent="Base.Theme.WanReader" />
</resources>
```

`WanReader/app/src/main/res/xml/backup_rules.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Android 12 以下的云备份规则占位（配合 Manifest 的 fullBackupContent） -->
<full-backup-content>
</full-backup-content>
```

`WanReader/app/src/main/res/xml/data_extraction_rules.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Android 12+ 的备份/迁移规则占位（配合 Manifest 的 dataExtractionRules） -->
<data-extraction-rules>
    <cloud-backup>
    </cloud-backup>
</data-extraction-rules>
```

- [ ] **Step 7: 复制启动图标资源（自 demo_03，S0 不做品牌设计）**

```bash
SRC=/Users/wenbo/Desktop/WBAndroid/Demo/demo_03/app/src/main/res
DST=/Users/wenbo/Desktop/WBAndroid/WanReader/app/src/main/res
mkdir -p "$DST/drawable"
cp "$SRC/drawable/ic_launcher_background.xml" "$DST/drawable/"
cp "$SRC/drawable/ic_launcher_foreground.xml" "$DST/drawable/"
cp -R "$SRC/mipmap-anydpi-v26" "$SRC/mipmap-hdpi" "$SRC/mipmap-mdpi" "$SRC/mipmap-xhdpi" "$SRC/mipmap-xxhdpi" "$SRC/mipmap-xxxhdpi" "$DST/"
```

- [ ] **Step 8: 验证 app 模块**

```bash
cd /Users/wenbo/Desktop/WBAndroid
# 所有 XML 可解析
for f in $(find WanReader/app/src/main -name "*.xml" ! -path "*mipmap*"); do
  python3 -c "import xml.etree.ElementTree as ET; ET.parse('$f')" || echo "BAD XML: $f"
done; echo "XML OK"
# 包目录与 namespace 一致、关键文件齐全
ls WanReader/app/src/main/java/com/wb/wanreader/MainActivity.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Theme.kt \
   WanReader/app/build.gradle.kts
# 图标资源齐全（6 个 mipmap 目录 + 2 个 drawable）
ls WanReader/app/src/main/res | grep -c mipmap
```

Expected: `XML OK` 且无 `BAD XML` 行；ls 列出 3 个文件；mipmap 目录计数为 `6`。

- [ ] **Step 9: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/app
git commit -m "WanReader S0：app 模块最小可运行模板（Compose + Material3 主题）"
```

---

### Task 3: S0 教学文档

**Files:**
- Create: `WanReader/doc/S0_工程初始化与配置.md`

**Interfaces:**
- Consumes: Task 1/2 的全部文件（文档逐一讲解它们）
- Produces: 文档末尾的"验收 checklist"，Task 5 学员验收以它为准

- [ ] **Step 1: 写教学文档**

文档必须包含以下全部章节与要素（iOS 视角、中文）：

1. **本课目标与成果**：一句话目标 + 跑通后的截图位说明
2. **Android 工程 vs Xcode 工程全景对照表**——至少含以下行：

| iOS | Android | 说明 |
|---|---|---|
| .xcodeproj / project.pbxproj | settings.gradle.kts + 各 build.gradle.kts | 工程结构由脚本描述，可 diff 可 review |
| Bundle Identifier | applicationId | 商店唯一身份 |
| Info.plist | AndroidManifest.xml | 组件需显式注册是最大差异 |
| Base SDK / Deployment Target | compileSdk / minSdk | 另有 iOS 没有的 targetSdk，需讲透三者关系 |
| CFBundleVersion / ShortVersionString | versionCode / versionName | |
| SPM Package.swift | gradle/libs.versions.toml | 版本单一真源 |
| xcconfig | gradle.properties | 全局构建开关 |
| Xcode 自带工具链 | gradle wrapper（gradlew） | 构建工具本身也被版本锁定，团队一致 |

3. **Gradle 全家桶逐文件讲解**：settings.gradle.kts（模块清单/插件版本真源/仓库管控）→ 根 build.gradle.kts（apply false 的含义）→ gradle.properties → libs.versions.toml（[versions]/[libraries]/[plugins] 三段结构）→ app/build.gradle.kts（android{} 每个块）→ wrapper 是什么、为何提交 jar。每个文件先给"一句话定位"，再逐块解释，块内引用 Task 1/2 的真实代码片段
4. **AGP ↔ Kotlin ↔ compileSdk ↔ JDK 版本关系**：谁约束谁、到哪里查官方兼容表、本工程四者的锁定值及理由；教"自己判断版本兼容性"的方法（授人以渔）
5. **坑与解决方案**（本课 3 个，均写清 报错原文 → 原因 → 解法）：
   - 坑① Kotlin 插件重复注入：`extension 'kotlin' already registered` / `already on the classpath with an unknown version`（demo_02 两次踩坑实录）
   - 坑② Gradle/依赖下载慢或超时：镜像仓库写法（腾讯/阿里镜像加在 `dependencyResolutionManagement.repositories` 首位）与 wrapper distributionUrl 镜像替换；附"用完建议还原"的提醒
   - 坑③ JDK 版本不匹配：`Unsupported class file major version` 类报错；AS 内置 JBR、Gradle JVM 设置入口（Settings → Build Tools → Gradle）、foojay 工具链插件兜底
6. **iOS 开发者最容易懵的 3 个点**：为什么有两个 build.gradle.kts；为什么改配置要 Sync（Sync 到底做了什么）；local.properties 为什么不进 git
7. **验收 checklist**（学员逐项打勾）：
   - [ ] Android Studio 打开 `WanReader/`（注意：打开 WanReader 目录本身，不是仓库根目录），Sync 成功无红字
   - [ ] 真机/模拟器运行，屏幕居中显示"WanReader 工程初始化成功 🎉"
   - [ ] 系统切深色模式，App 背景/文字颜色跟随变化
   - [ ] 能对照文档说出 settings.gradle.kts / 根 build / app build / libs.versions.toml 各自职责（自测）
   - [ ] `git status` 确认 `WanReader/local.properties`、`.gradle/`、`build/` 未被跟踪

- [ ] **Step 2: 验证文档完整性**

```bash
cd /Users/wenbo/Desktop/WBAndroid
# 必备小节齐全
for kw in "对照" "settings.gradle.kts" "libs.versions.toml" "坑" "验收"; do
  grep -q "$kw" "WanReader/doc/S0_工程初始化与配置.md" || echo "缺少小节关键词: $kw"
done; echo "DOC OK"
# 无占位符
grep -nE "TBD|TODO" "WanReader/doc/S0_工程初始化与配置.md" && echo "存在占位符" || echo "NO PLACEHOLDER"
```

Expected: `DOC OK` 且无"缺少小节"行；`NO PLACEHOLDER`。

- [ ] **Step 3: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/doc
git commit -m "WanReader S0：教学文档（Gradle 全家桶详解 + 版本关系 + 三大坑）"
```

---

### Task 4: 教学进度文件

**Files:**
- Create: `WanReader/教学进度.md`

**Interfaces:**
- Consumes: 设计文档的 11 阶段表（`docs/superpowers/specs/2026-07-12-wanreader-teaching-design.md` 第 5 节）
- Produces: 后续每次会话开始必读、结束必更新的进度真源

- [ ] **Step 1: 写 `WanReader/教学进度.md`**

```markdown
# WanReader 教学进度（单一真源 · 每节课必更新）

> 规则：会话开始先读本文件定位 `⬜`/`🔄`；会话结束更新总览表 + 追加当日日志 + 写 next step。
> 本进度独立于仓库根目录的 `学习进度.md`（原 4 周路线），互不影响。
> 设计文档：`docs/superpowers/specs/2026-07-12-wanreader-teaching-design.md`

## 总览表

| # | 阶段 | 状态 | 完成日 | 教学文档 |
|---|---|---|---|---|
| S0 | 工程初始化与配置 | 🔄 待真机验收 | — | doc/S0_工程初始化与配置.md |
| S1 | 架构骨架（分层 + Hilt + 导航） | ⬜ | — | — |
| S2 | 网络层（Retrofit + 统一封装） | ⬜ | — | — |
| S3 | 首页列表（Paging 3 + 三态） | ⬜ | — | — |
| S4 | 登录与会话（Cookie + DataStore） | ⬜ | — | — |
| S5 | 详情与收藏（WebView + 乐观更新） | ⬜ | — | — |
| S6 | 本地缓存（Room + 离线优先） | ⬜ | — | — |
| S7 | XML/View 专题 | ⬜ | — | — |
| S8 | 多模块演进 | ⬜ | — | — |
| S9 | 构建变体与多渠道 | ⬜ | — | — |
| S10 | 分发与上架 | ⬜ | — | — |

图例：✅ 完成 / 🔄 进行中 / ⬜ 未开始

## 日期日志

### 2026-07-12
- **完成**：教学设计定稿并提交（11 阶段方案 A）；S0 交付——`WanReader/` 工程骨架（AGP 9.2.1 / Kotlin 2.3.20 / Gradle 9.4.1 / compileSdk 36，复用 demo_03 已验证组合）、app 模块最小 Compose 模板、教学文档 S0。
- **预埋的坑已在文档讲解**：Kotlin 插件重复注入 / 下载镜像 / JDK 不匹配。
- **待办**：学员本地 Android Studio 打开 `WanReader/` Sync + 真机跑通，按 S0 文档末尾 checklist 验收。

## next step

学员完成 S0 验收 checklist → 标记 S0 ✅ → 开始 S1（分层包结构 + Hilt 接入 + 底部三 Tab 导航骨架）。
```

- [ ] **Step 2: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/教学进度.md
git commit -m "WanReader S0：建立教学进度文件（11 阶段总览）"
```

---

### Task 5: 学员真机验收（门禁，不可跳过）

**Files:**
- Modify: `WanReader/教学进度.md`（验收通过后更新）

**Interfaces:**
- Consumes: Task 3 文档末尾的验收 checklist
- Produces: S0 ✅ 状态；S1 开工许可

- [ ] **Step 1: 请学员执行验收**

请学员在本地完成（沙箱无 Android SDK，此步只能由学员执行）：

1. Android Studio → Open → 选择 `WBAndroid/WanReader` 目录（不是仓库根目录）
2. 等待 Sync 完成；若报错，把完整报错贴回会话现场排查（排查过程记入 S0 文档"坑"小节）
3. 连真机或启动模拟器，Run 'app'
4. 按 `WanReader/doc/S0_工程初始化与配置.md` 末尾 checklist 逐项确认

- [ ] **Step 2: 验收通过后更新进度**

将 `WanReader/教学进度.md` 总览表 S0 行改为 `✅ 完成`、填写完成日期，日志追加验收结果，next step 改为 S1 开工。

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/教学进度.md
git commit -m "WanReader S0：真机验收通过，S0 完成"
```

Expected: 学员逐项确认 checklist 后提交；若验收失败，先修复再验收，不进入 S1。

---

## Self-Review 记录

1. **Spec 覆盖**：S0 范围（工程创建、Gradle 逐文件讲解、版本关系、.gitignore、三个坑、教学文档、进度文件、真机验收）均有对应 Task；"每节课必有配套文档"由 Task 3 落实。S1+ 内容（Hilt/分层/导航）明确排除，符合 YAGNI。
2. **占位符扫描**：全部文件给出完整内容或完整内容要素清单；无 TBD/TODO。
3. **一致性**：包名 `com.wb.wanreader` 与目录 `java/com/wb/wanreader/` 一致；Manifest 主题 `Theme.WanReader` 与 themes.xml 定义一致；`WanReaderTheme` 在 Task 2 Step 4 定义、Step 5 引用，签名一致；Catalog 别名（Task 1 Step 5）与 app 依赖引用（Task 2 Step 1）逐一对应；TDD 在本任务中体现为"每 Task 自带可执行验证步骤"，编译级验证受沙箱限制由 Task 5 门禁承担。
