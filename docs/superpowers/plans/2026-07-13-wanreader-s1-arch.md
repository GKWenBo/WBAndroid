# WanReader S1 · 架构骨架 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 S0 模板上落地企业级架构骨架：分层包结构 + Hilt 依赖注入 + 品牌主题 + 底部三 Tab 导航，真机验收"三 Tab 可切换 + 跟随深色模式 + Hilt 注入链路可见"。

**Architecture:** 单 app 模块内落地官方架构分层包（ui/domain/data/di/util）；Hilt 走 KSP2 编译期注入，用"Module 提供 → Repository 构造注入 → @HiltViewModel → UI"完整链路做最小示范；导航用 navigation-compose 字符串路由 + Material3 NavigationBar。

**Tech Stack:** Hilt 2.60.1（2.59 起支持 AGP 9）、KSP 2.3.9（KSP2，已与 Kotlin 版本解耦）、androidx.hilt:hilt-navigation-compose 1.4.0、navigation-compose / lifecycle-viewmodel-compose（Compose BOM 2025.06.00 管理，沿用 demo_03 已验证模式）。

## Global Constraints

- 版本锁定（已联网核实，2026-07-13）：`ksp = "2.3.9"`、`hilt = "2.60.1"`、`hiltNavigationCompose = "1.4.0"`；其余沿用 S0（AGP 9.2.1 / Kotlin 2.3.20 / Gradle 9.4.1 / compileSdk 36 / BOM 2025.06.00）
- **模块 `plugins` 块仍禁止声明 `id("org.jetbrains.kotlin.android")`**（S0 坑①）
- **必须用 KSP，禁止 kapt**：KSP1/kapt 路线不兼容 Kotlin 2.3+ 与 AGP 9+（本课坑，写入教学文档）
- 包名 `com.wb.wanreader`；新依赖一律进 Version Catalog，禁止裸版本字符串
- 沙箱无 Android SDK：计划内验证 = 语法解析 + 结构比对；编译/真机验收由学员本地完成（Task 7 门禁）
- git 提交信息不加 Co-Authored-By 行
- 教学文档 `WanReader/doc/S1_架构骨架与Hilt.md`（每节课必有配套文档）

---

### Task 1: 依赖与插件接入

**Files:**
- Modify: `WanReader/gradle/libs.versions.toml`
- Modify: `WanReader/build.gradle.kts`
- Modify: `WanReader/app/build.gradle.kts`

**Interfaces:**
- Produces: 插件别名 `libs.plugins.ksp`、`libs.plugins.hilt`；库别名 `libs.hilt.android`、`libs.hilt.android.compiler`、`libs.androidx.hilt.navigation.compose`、`libs.androidx.navigation.compose`、`libs.androidx.lifecycle.viewmodel.compose`，供 Task 2/4 的代码 import 对应 API

- [ ] **Step 1: `libs.versions.toml` 增量**

`[versions]` 追加：

```toml
# S1 新增：DI 与导航（版本于 2026-07-13 联网核实）
ksp = "2.3.9"          # KSP2：版本已与 Kotlin 解耦；KSP1 不兼容 Kotlin 2.3+/AGP 9+
hilt = "2.60.1"        # Hilt 2.59 起才支持 AGP 9（配套要求 Gradle 9.1+，本工程 9.4.1 满足）
hiltNavigationCompose = "1.4.0"
```

`[libraries]` 追加：

```toml
# S1 新增：Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-android-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
# S1 新增：导航与 ViewModel（版本由 Compose BOM 管理）
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose" }
```

`[plugins]` 追加：

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 2: 根 `build.gradle.kts` 登记插件（apply false）**

整文件改为：

```kotlin
// 根 build.gradle.kts —— 只做一件事：把插件"登记"到类路径但不应用（apply false），
// 由各模块自行选用。iOS 对照：≈ Podfile 顶部的平台/源声明，不含具体 target 配置。
plugins {
    alias(libs.plugins.android.application) apply false
    // S1 新增：KSP（注解处理）与 Hilt（DI），版本在此仲裁，模块自行应用
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 3: `app/build.gradle.kts` 应用插件并加依赖**

`plugins` 块改为：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    // Compose 编译器插件（Kotlin 2.x 用它开启 @Composable 编译支持）。
    // 坑①提醒：不要再加 org.jetbrains.kotlin.android —— AGP 9.2.1 会自动应用，
    // 重复声明报 extension 'kotlin' already registered。
    alias(libs.plugins.kotlin.compose)
    // S1 新增：KSP 处理 Hilt 注解（必须 KSP，kapt 不兼容 Kotlin 2.3+/AGP 9+）
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
```

`dependencies` 块末尾（`debugImplementation` 之前）追加：

```kotlin
    // S1 新增：导航与 ViewModel（BOM 管理版本）
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // S1 新增：Hilt —— implementation 是运行时库，ksp 是编译期代码生成器
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
```

- [ ] **Step 4: 验证**

```bash
cd /Users/wenbo/Desktop/WBAndroid
python3 -c "import tomllib; d=tomllib.load(open('WanReader/gradle/libs.versions.toml','rb')); assert 'hilt' in d['versions'] and 'ksp' in d['plugins']; print('TOML OK')"
grep -q "libs.plugins.hilt" WanReader/build.gradle.kts && grep -q "ksp(libs.hilt.android.compiler)" WanReader/app/build.gradle.kts && echo "GRADLE OK"
grep -c "org.jetbrains.kotlin.android" WanReader/app/build.gradle.kts || echo "NO KOTLIN PLUGIN OK"
```

Expected: `TOML OK`、`GRADLE OK`；最后一条输出注释中提及的次数（grep 到的是注释文字），确认 `plugins` 块无实际声明。

- [ ] **Step 5: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/gradle/libs.versions.toml WanReader/build.gradle.kts WanReader/app/build.gradle.kts
git commit -m "WanReader S1：接入 KSP 2.3.9 + Hilt 2.60.1 + 导航依赖"
```

---

### Task 2: Hilt 骨架与最小注入链路

**Files:**
- Create: `WanReader/app/src/main/java/com/wb/wanreader/WanApp.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/di/AppModule.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/data/AppInfoRepository.kt`
- Modify: `WanReader/app/src/main/AndroidManifest.xml`（application 注册 WanApp）

**Interfaces:**
- Consumes: Task 1 的 Hilt 依赖
- Produces: `AppVersionName(val value: String)`（value class，包 `com.wb.wanreader.di`）；`AppInfoRepository.appDescription(): String`（包 `com.wb.wanreader.data`），Task 4 的 HomeViewModel 注入使用

- [ ] **Step 1: 写 `WanApp.kt`**

```kotlin
package com.wb.wanreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp：Hilt 的总开关——编译期生成全 App 的依赖容器（SingletonComponent），
// 并让 Application 持有它。iOS 对照：≈ 在 AppDelegate/App 入口手工组装的"依赖工厂"，
// 差别是 Hilt 在【编译期】生成这套工厂代码，写错了直接编译失败而不是运行时崩。
@HiltAndroidApp
class WanApp : Application()
```

- [ ] **Step 2: 写 `di/AppModule.kt`**

```kotlin
package com.wb.wanreader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 强类型包一层，避免直接注入裸 String（多个 String 依赖会撞车，这是 DI 的通用规范）
@JvmInline
value class AppVersionName(val value: String)

// @Module：告诉 Hilt "这里有一批依赖的制造方法"。
// @InstallIn(SingletonComponent::class)：装进全 App 生命周期的容器（≈ 全局单例作用域）。
// 什么时候用 @Provides？——当依赖【不是你写的类】（系统 API、三方库、需要加工才能得到）
// 时，没法在它构造函数上标 @Inject，就在 Module 里写个方法教 Hilt 怎么造。
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // @ApplicationContext：Hilt 内置的限定符，注入全局 Context（而非某个 Activity 的）
    @Provides
    @Singleton
    fun provideAppVersionName(@ApplicationContext context: Context): AppVersionName {
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: "unknown"
        return AppVersionName(versionName)
    }
}
```

- [ ] **Step 3: 写 `data/AppInfoRepository.kt`**

```kotlin
package com.wb.wanreader.data

import com.wb.wanreader.di.AppVersionName
import javax.inject.Inject
import javax.inject.Singleton

// 构造注入：依赖【是你自己写的类】时，直接在构造函数标 @Inject，
// Hilt 就知道"要造 AppInfoRepository，先造它构造函数要的东西"——不需要写 Module。
// 这是 Hilt 的主力用法，Module 的 @Provides 只兜底造不了的。
// iOS 对照：≈ init(versionName:) 手工传参，只是"谁来调 init"从你变成了 Hilt。
@Singleton
class AppInfoRepository @Inject constructor(
    private val versionName: AppVersionName
) {
    fun appDescription(): String = "WanReader v${versionName.value} · Hilt 注入链路已打通"
}
```

- [ ] **Step 4: Manifest 注册 WanApp**

`AndroidManifest.xml` 的 `<application` 标签增加一行属性（放在 `android:allowBackup` 之前）：

```xml
    <application
        android:name=".WanApp"
        android:allowBackup="true"
```

- [ ] **Step 5: 验证**

```bash
cd /Users/wenbo/Desktop/WBAndroid
python3 -c "import xml.etree.ElementTree as ET; ET.parse('WanReader/app/src/main/AndroidManifest.xml')" && grep -q 'android:name=".WanApp"' WanReader/app/src/main/AndroidManifest.xml && echo "MANIFEST OK"
ls WanReader/app/src/main/java/com/wb/wanreader/WanApp.kt \
   WanReader/app/src/main/java/com/wb/wanreader/di/AppModule.kt \
   WanReader/app/src/main/java/com/wb/wanreader/data/AppInfoRepository.kt
```

Expected: `MANIFEST OK` + 3 个文件路径。

- [ ] **Step 6: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/app
git commit -m "WanReader S1：Hilt 骨架（WanApp + AppModule + 构造注入示例仓库）"
```

---

### Task 3: 品牌主题（Color/Type/Theme）

**Files:**
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Color.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Type.kt`
- Modify: `WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Theme.kt`

**Interfaces:**
- Produces: `WanReaderTheme(...)` 签名不变（S0 已定义），内部改用品牌配色；Task 4 各 Screen 直接包裹使用

- [ ] **Step 1: 写 `Color.kt`**

```kotlin
package com.wb.wanreader.ui.theme

import androidx.compose.ui.graphics.Color

// 品牌色：玩Android 生态惯用的蓝色系。命名遵循 Material3 语义（不叫 blue500 叫 primary 系）
val WanBlue = Color(0xFF1565C0)
val WanBlueDark = Color(0xFF90CAF9)
val WanBlueContainer = Color(0xFFD6E4FF)
val WanBlueContainerDark = Color(0xFF274777)
```

- [ ] **Step 2: 写 `Type.kt`**

```kotlin
package com.wb.wanreader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// S1 只定制列表页会用到的两档，其余用 Material3 默认。iOS 对照：≈ UIFont.preferredFont 语义档位
val WanTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

- [ ] **Step 3: 改 `Theme.kt`（整文件替换）**

```kotlin
package com.wb.wanreader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// App 级主题包装。iOS 对照：≈ SwiftUI 根视图上统一注入的 environment 配色。
// S1 升级：接入品牌配色与字体档；深浅色两套 scheme 跟随系统切换。
private val LightColors = lightColorScheme(
    primary = WanBlue,
    primaryContainer = WanBlueContainer
)

private val DarkColors = darkColorScheme(
    primary = WanBlueDark,
    primaryContainer = WanBlueContainerDark
)

@Composable
fun WanReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WanTypography,
        content = content
    )
}
```

- [ ] **Step 4: 验证 + Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
ls WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Color.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Type.kt
grep -q "WanTypography" WanReader/app/src/main/java/com/wb/wanreader/ui/theme/Theme.kt && echo "THEME OK"
git add WanReader/app && git commit -m "WanReader S1：品牌主题（蓝色系配色 + 字体档 + 深浅色 scheme）"
```

Expected: 2 个文件路径 + `THEME OK`，提交成功。

---

### Task 4: 底部三 Tab 导航 + Hilt 链路上屏

**Files:**
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/navigation/WanDestination.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/home/HomeViewModel.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/home/HomeScreen.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/square/SquareScreen.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/mine/MineScreen.kt`
- Create: `WanReader/app/src/main/java/com/wb/wanreader/ui/MainScreen.kt`
- Modify: `WanReader/app/src/main/java/com/wb/wanreader/MainActivity.kt`

**Interfaces:**
- Consumes: `AppInfoRepository.appDescription(): String`（Task 2）；`WanReaderTheme`（Task 3）
- Produces: `MainScreen()` 顶层 Composable（MainActivity 唯一入口）；`WanDestination` enum（route/label/icon），S2+ 增页面时扩展它

- [ ] **Step 1: 写 `ui/navigation/WanDestination.kt`**

```kotlin
package com.wb.wanreader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

// 底部 Tab 的路由表：route 是导航系统里的"URL"，唯一标识一个页面。
// iOS 对照：≈ TabView 的 tag + NavigationPath 的目的地类型，集中定义避免散落魔法字符串。
enum class WanDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "首页", Icons.Filled.Home),
    SQUARE("square", "广场", Icons.Filled.Star),
    MINE("mine", "我的", Icons.Filled.Person)
}
```

- [ ] **Step 2: 写 `ui/home/HomeViewModel.kt`**

```kotlin
package com.wb.wanreader.ui.home

import androidx.lifecycle.ViewModel
import com.wb.wanreader.data.AppInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// @HiltViewModel + @Inject constructor：Hilt 接管 ViewModel 的创建，
// 构造函数里要什么依赖就写什么，UI 层完全不知道 Repository 的存在。
// iOS 对照：≈ MVVM 里 ViewModel 的 init 注入，只是实例化交给了框架（配合 hiltViewModel()）。
// S1 先返回静态文案验证链路；S3 起这里会变成 StateFlow + 分页数据流。
@HiltViewModel
class HomeViewModel @Inject constructor(
    appInfoRepository: AppInfoRepository
) : ViewModel() {
    val greeting: String = appInfoRepository.appDescription()
}
```

- [ ] **Step 3: 写 `ui/home/HomeScreen.kt`**

```kotlin
package com.wb.wanreader.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// hiltViewModel()：从 Hilt 容器里按当前导航目的地的生命周期取/建 ViewModel。
// iOS 对照：≈ SwiftUI 的 @StateObject 交给 DI 容器来 new。
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "首页", style = MaterialTheme.typography.titleLarge)
        Text(
            text = viewModel.greeting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
```

- [ ] **Step 4: 写 `ui/square/SquareScreen.kt` 与 `ui/mine/MineScreen.kt`**

`SquareScreen.kt`：

```kotlin
package com.wb.wanreader.ui.square

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// S1 占位页；S3 后接入项目/广场列表
@Composable
fun SquareScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "广场（S3 接入列表）", style = MaterialTheme.typography.titleLarge)
    }
}
```

`MineScreen.kt`：

```kotlin
package com.wb.wanreader.ui.mine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// S1 占位页；S4 接入登录态与用户信息
@Composable
fun MineScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "我的（S4 接入登录）", style = MaterialTheme.typography.titleLarge)
    }
}
```

- [ ] **Step 5: 写 `ui/MainScreen.kt`**

```kotlin
package com.wb.wanreader.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wb.wanreader.ui.home.HomeScreen
import com.wb.wanreader.ui.mine.MineScreen
import com.wb.wanreader.ui.navigation.WanDestination
import com.wb.wanreader.ui.square.SquareScreen

// App 的 UI 根：Scaffold(底部导航条) + NavHost(页面容器)。
// iOS 对照：≈ TabView { NavigationStack { ... } }，但 Android 用"单 NavHost + 路由"而非三个独立栈。
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 订阅当前路由，用于底部条选中态（≈ TabView 的 selection 绑定）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                WanDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Tab 切换三件套（企业项目标准写法）：
                                // 1) 回退到起始页再切，避免 Tab 间叠出深栈
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 2) 连点同一 Tab 不重复入栈
                                launchSingleTop = true
                                // 3) 恢复该 Tab 上次的浏览状态
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WanDestination.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(WanDestination.HOME.route) { HomeScreen() }
            composable(WanDestination.SQUARE.route) { SquareScreen() }
            composable(WanDestination.MINE.route) { MineScreen() }
        }
    }
}
```

- [ ] **Step 6: 改 `MainActivity.kt`（整文件替换）**

```kotlin
package com.wb.wanreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wb.wanreader.ui.MainScreen
import com.wb.wanreader.ui.theme.WanReaderTheme
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint：让这个 Activity 能接住 Hilt 容器里的依赖
// （hiltViewModel() 能工作的前提）。忘加它是 Hilt 最高频报错，见 S1 文档坑②。
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WanReaderTheme {
                MainScreen()
            }
        }
    }
}
```

- [ ] **Step 7: 验证**

```bash
cd /Users/wenbo/Desktop/WBAndroid
ls WanReader/app/src/main/java/com/wb/wanreader/ui/navigation/WanDestination.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/home/HomeViewModel.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/home/HomeScreen.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/square/SquareScreen.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/mine/MineScreen.kt \
   WanReader/app/src/main/java/com/wb/wanreader/ui/MainScreen.kt
grep -q "@AndroidEntryPoint" WanReader/app/src/main/java/com/wb/wanreader/MainActivity.kt && \
grep -q "@HiltViewModel" WanReader/app/src/main/java/com/wb/wanreader/ui/home/HomeViewModel.kt && echo "HILT WIRING OK"
# S0 的 HelloScreen 已被 MainScreen 取代，确认无残留引用
grep -rn "HelloScreen" WanReader/app/src/main/java || echo "NO HELLO RESIDUE"
```

Expected: 6 个文件路径 + `HILT WIRING OK` + `NO HELLO RESIDUE`。

- [ ] **Step 8: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/app
git commit -m "WanReader S1：底部三 Tab 导航 + HomeViewModel 注入链路上屏"
```

---

### Task 5: S1 教学文档

**Files:**
- Create: `WanReader/doc/S1_架构骨架与Hilt.md`

**Interfaces:**
- Consumes: Task 1~4 的全部代码（文档逐一讲解）
- Produces: 文末验收 checklist，Task 7 验收以它为准

- [ ] **Step 1: 写教学文档**

必须包含以下章节与要素（中文、iOS 视角）：

1. **本课目标与成果**：分层骨架 + Hilt 链路 + 三 Tab 导航，真机效果描述
2. **分层包结构讲解**：`ui/ domain/ data/ di/ util/` 各层职责表 + 与 iOS 分层（View/ViewModel/Service/Repository）对照 + "为什么 UI 不许直接 import data 层实现"的依赖方向规则（S8 拆多模块的伏笔）
3. **Hilt 核心概念 iOS 视角拆解**（本课重头戏）：
   - 为什么需要 DI：从"手工在 init 里 new 依赖"到"编译期生成工厂"的动机（可测试性、单例管控、作用域）
   - 五个注解各配真实代码引用讲解：`@HiltAndroidApp`（总开关）、`@AndroidEntryPoint`（接入点）、`@Inject constructor`（主力：自己的类）、`@Module + @Provides + @InstallIn`（兜底：造不了的类）、`@HiltViewModel + hiltViewModel()`（ViewModel 专用通道）
   - 组件层级图：SingletonComponent → ActivityComponent → ViewModelComponent 的作用域含义（≈ 全局单例 / 页面级生命周期）
   - "Hilt 在编译期做了什么"：指出 build 目录下生成代码的位置，鼓励学员看一眼生成的工厂类
4. **kapt vs KSP**（本课坑①）：kapt 是什么（Java 注解处理器的 Kotlin 桥）、为什么被 KSP 取代（速度约 2 倍）、**硬约束：KSP1/kapt 不兼容 Kotlin 2.3+ 与 AGP 9+，本工程别无选择**；KSP2 起版本与 Kotlin 解耦（2.3.9 通用），不再需要 `<kotlin版本>-<ksp版本>` 对表
5. **Hilt 报错排查套路**（本课坑②，给出报错原文 → 原因 → 解法）：
   - `[Dagger/MissingBinding] XXX cannot be provided`：依赖图里没人会造 XXX → 检查是否漏了 @Inject constructor 或 Module 里的 @Provides
   - `@HiltViewModel ... does not have @AndroidEntryPoint`（或运行时 `Cannot create an instance of ViewModel`）：宿主 Activity/Fragment 忘标 @AndroidEntryPoint
   - `Hilt Android Gradle plugin is applied but no com.google.dagger:hilt-android-compiler dependency`：加了插件忘加 ksp 编译器依赖
   - 通用心法：Hilt 的报错都在【编译期】，报错信息里必然带着"哪条依赖链断了"，从下往上读
6. **导航三件套讲解**：`popUpTo+saveState / launchSingleTop / restoreState` 为什么是 Tab 切换标准写法（对照 iOS TabView 天然的多栈行为，Android 需要显式配置）
7. **验收 checklist**：
   - [ ] Sync 成功（首次会下载 Hilt/KSP，若慢参考 S0 坑②镜像）
   - [ ] 真机运行：底部三 Tab（首页/广场/我的）可切换
   - [ ] 首页显示 "WanReader v0.1.0 · Hilt 注入链路已打通"（版本号来自 PackageManager，证明 Module→Repository→ViewModel→UI 全链路）
   - [ ] 深色模式切换：主题色跟随（注意对比 S0：现在主色是品牌蓝）
   - [ ] 自测：能说出 @Inject constructor 和 @Provides 分别什么时候用

- [ ] **Step 2: 验证 + Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
for kw in "分层" "@HiltAndroidApp" "@Provides" "KSP" "MissingBinding" "验收"; do
  grep -q "$kw" "WanReader/doc/S1_架构骨架与Hilt.md" || echo "缺少小节关键词: $kw"
done; echo "DOC OK"
grep -nE "TBD|TODO" "WanReader/doc/S1_架构骨架与Hilt.md" && echo "存在占位符" || echo "NO PLACEHOLDER"
git add WanReader/doc && git commit -m "WanReader S1：教学文档（分层架构 + Hilt iOS 视角拆解 + kapt/KSP + 排错套路）"
```

Expected: `DOC OK` 无缺少行、`NO PLACEHOLDER`，提交成功。

---

### Task 6: 更新教学进度

**Files:**
- Modify: `WanReader/教学进度.md`

- [ ] **Step 1: 更新进度文件**

总览表 S1 行改为：

```markdown
| S1 | 架构骨架（分层 + Hilt + 导航） | 🔄 待真机验收 | — | doc/S1_架构骨架与Hilt.md |
```

日期日志追加（如当日已有条目则合并进去）：

```markdown
### 2026-07-13（S1）
- **完成**：S1 交付——KSP 2.3.9 + Hilt 2.60.1 接入（版本已联网核实：Hilt 2.59 起支持 AGP 9；KSP1 不兼容 Kotlin 2.3+）；分层包结构（ui/di/data）；品牌蓝主题；底部三 Tab 导航；Home 页展示 Module→Repository→ViewModel→UI 完整注入链路。
- **教学文档**：doc/S1_架构骨架与Hilt.md（Hilt 五注解 iOS 视角拆解 + kapt/KSP 坑 + 排错套路）。
- **待办**：学员本地 Sync（首次下载 Hilt/KSP 依赖）+ 真机验收 S1 文档 checklist。
```

next step 改为：

```markdown
学员完成 S1 验收 checklist → 标记 S1 ✅ → 开始 S2（网络层：Retrofit + OkHttp + kotlinx.serialization + BaseResponse 统一封装）。
```

- [ ] **Step 2: Commit**

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/教学进度.md
git commit -m "WanReader S1：更新教学进度（待真机验收）"
```

---

### Task 7: 学员真机验收（门禁，不可跳过）

**Files:**
- Modify: `WanReader/教学进度.md`（验收通过后更新）

- [ ] **Step 1: 请学员执行验收**

1. Android Studio 打开 `WanReader/`，Sync（首次会下载 Hilt/KSP，可能较慢）
2. 真机 Run，按 `doc/S1_架构骨架与Hilt.md` 文末 checklist 逐项确认
3. 报错则贴回会话现场排查，排查过程补录进文档坑小节

- [ ] **Step 2: 验收通过后更新进度并提交**

S1 行改 `✅ 完成` + 完成日期；日志追加验收结果；next step 指向 S2。

```bash
cd /Users/wenbo/Desktop/WBAndroid
git add WanReader/教学进度.md
git commit -m "WanReader S1：真机验收通过，S1 完成"
```

---

## Self-Review 记录

1. **Spec 覆盖**：S1 范围（分层包结构、Hilt 接入、Material3 主题+深色、Navigation 底部三 Tab、kapt/ksp 坑、Hilt 排错套路、验收标准"三 Tab 切换+深色模式"）全部有对应 Task；额外把"Hilt 链路可见"做成首页文案，让验收从"能跑"升级为"能证明 DI 工作"。
2. **占位符扫描**：所有代码完整给出；文档任务为逐节内容要素清单，无 TBD/TODO。
3. **类型一致性**：`AppVersionName`（Task 2 定义/消费一致）；`AppInfoRepository.appDescription()`（Task 2 Produces = Task 4 HomeViewModel 消费）；`WanReaderTheme` 签名与 S0 一致（Task 3 只改内部）；`WanDestination.entries`（Kotlin 1.9+ enum entries，Kotlin 2.3.20 支持）；版本三元组（ksp 2.3.9 / hilt 2.60.1 / hiltNavigationCompose 1.4.0）已于 2026-07-13 联网核实。
