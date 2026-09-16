# S1 · 架构骨架与 Hilt（iOS 视角）

> WanReader 教学系列第 1 课。目标：**分层包结构落地 + Hilt 依赖注入打通 + 底部三 Tab 导航**。
> 跑通后：App 有了品牌蓝主题和三个 Tab（首页/广场/我的），首页显示 "WanReader v0.1.0 · Hilt 注入链路已打通"——这行字走完了 Module → Repository → ViewModel → UI 的完整依赖注入链路。

---

## 1. 本课目标与成果

一句话目标：把 S0 的"Hello World 模板"升级为"有架构的 App 骨架"，后续所有功能（S2 网络、S3 列表、S4 登录……）都长在这副骨架上。

成果物：
- 分层包结构：`ui/ di/ data/`（domain 层 S3 引入 UseCase 时补齐）
- Hilt 2.60.1 + KSP 2.3.9 接入，一条可见的注入链路
- 品牌蓝主题（深浅色两套 scheme）
- 底部三 Tab 导航（navigation-compose）

---

## 2. 分层包结构：依赖方向是铁律

```
com.wb.wanreader
├── WanApp.kt            # Application（Hilt 总开关）
├── MainActivity.kt      # 唯一 Activity（@AndroidEntryPoint）
├── ui/                  # UI 层：只认 ViewModel，不认 Repository
│   ├── MainScreen.kt    #   UI 根：底部导航 + NavHost
│   ├── navigation/      #   路由表
│   ├── home/ square/ mine/   # 按功能分包：Screen + ViewModel 放一起
│   └── theme/           #   Color/Type/Theme
├── di/                  # DI 配置：Module 们住这里
├── data/                # 数据层：Repository + 数据源（S2 加 network/，S6 加 local/）
└── util/                # 通用工具（暂空）
```

| 层 | 职责 | iOS 对照 |
|---|---|---|
| ui | Composable + ViewModel，管"显示什么、响应什么" | SwiftUI View + ViewModel |
| domain | 业务规则、UseCase（可选薄层，S3 引入） | 业务 Service / Interactor |
| data | Repository 统一数据出入口，屏蔽"数据从哪来" | Repository / APIClient + 持久层封装 |
| di | 教 Hilt 造那些"造不了"的依赖 | 手工 DI 容器 / Factory |

**依赖方向铁律：`ui → domain → data`，只准向下，不准反向、不准跨层直连数据源。**
UI 永远不该 import OkHttp 或 Room 的类型——今天靠自觉遵守，S8 拆多模块后由编译器强制（ui 模块根本看不见 data 的实现类）。这就是"为什么现在就要分包"的答案：**包结构是未来模块边界的草图**。

**按功能分包（home/square/mine）而不是按类型分包（viewmodels/screens/）**——企业项目标准做法：改一个功能时相关文件都在一个目录里，也为 S8 拆 `feature-*` 模块铺路。iOS 对照：≈ 按 Feature 组织 group，而不是把所有 ViewModel 塞一个文件夹。

---

## 3. Hilt：iOS 开发者的依赖注入速成

### 3.1 为什么需要 DI 框架（iOS 没有它你也活得很好，为什么？）

iOS 里你大概这么写：`init(service: APIService = .shared)`——手工注入 + 单例默认值。小项目没问题，规模上来后三个痛点：

1. **组装代码爆炸**：A 依赖 B，B 依赖 C、D……每个 ViewModel 的创建点都要手写整条链
2. **单例失控**：`.shared` 满天飞，测试时换不掉、生命周期不可控
3. **改构造函数 = 全工程搜索替换**：给 Repository 加个依赖，所有 new 它的地方都要改

Hilt 的方案：你只声明"我需要什么"（构造函数参数）和"怎么造"（注解），**组装代码由编译器生成**。注意是编译期——依赖图缺了一环直接编译失败，而不是运行时崩（这是它比很多 iOS DI 库如 Swinject 强的点，后者是运行时解析）。

### 3.2 五个注解 = Hilt 的全部日常（对照本课真实代码）

**① `@HiltAndroidApp`（总开关）—— `WanApp.kt`**
标在 Application 上，编译期生成全 App 的依赖容器。没有它，其他注解全部失效。一个 App 只写一次。

**② `@AndroidEntryPoint`（接入点）—— `MainActivity.kt`**
标在 Activity/Fragment 上，声明"这个组件要从容器里取依赖"。`hiltViewModel()` 能工作的前提。

**③ `@Inject constructor`（主力，80% 场景）—— `AppInfoRepository.kt`**
自己写的类，构造函数标 `@Inject`，Hilt 就会造它，并递归地先造它的参数。**优先用这个**，不需要任何 Module。

```kotlin
@Singleton
class AppInfoRepository @Inject constructor(
    private val versionName: AppVersionName   // Hilt 会先去找"谁会造 AppVersionName"
)
```

**④ `@Module + @Provides + @InstallIn`（兜底，20% 场景）—— `di/AppModule.kt`**
造不了的类才用它：系统 API、三方库的类（S2 的 Retrofit/OkHttp 就靠它）、需要加工才能得到的值。

```kotlin
@Module
@InstallIn(SingletonComponent::class)   // 装进哪个作用域的容器
object AppModule {
    @Provides @Singleton
    fun provideAppVersionName(@ApplicationContext context: Context): AppVersionName { ... }
}
```

**⑤ `@HiltViewModel` + `hiltViewModel()`（ViewModel 专用通道）—— `HomeViewModel.kt` / `HomeScreen.kt`**
ViewModel 生命周期归系统管（要在旋转屏幕后存活），所以有专门通道：类上标 `@HiltViewModel`，UI 里用 `hiltViewModel()` 获取。iOS 对照：≈ `@StateObject` 的实例化交给 DI 容器。

### 3.3 作用域（组件层级）：@Singleton 到底"单"在哪

```
SingletonComponent      全 App 生命周期        ← AppModule 装在这，@Singleton = 全局单例
   └── ActivityRetainedComponent   跨配置变更存活（旋转屏幕不死）
         └── ViewModelComponent    跟随某个 ViewModel   ← @HiltViewModel 的依赖默认装这
               └── ActivityComponent / FragmentComponent   跟随页面生死
```

规则：**子作用域能拿父作用域的东西，反之不行**。`AppVersionName` 装在 Singleton 层，所以 ViewModel 层的 `HomeViewModel` 拿得到；如果反过来（Singleton 层的类想注入某个 Activity 的东西），编译报错。iOS 对照：≈ 环境注入的传播方向——App 级 environment 子视图都能读，子视图的 State 上层拿不到。

### 3.4 亲眼看看 Hilt 生成了什么（强烈建议做一次）

Sync + Build 之后，在 Android Studio 里切到 Project 视图，看这个目录：

```
WanReader/app/build/generated/ksp/debug/kotlin/   以及
WanReader/app/build/generated/hilt/
```

你会找到 `HomeViewModel_Factory`、`AppModule_ProvideAppVersionNameFactory` 这类生成类——这就是"编译期生成的组装代码"的实体。**DI 不是魔法，是代码生成**，看过一次生成物，Hilt 的心智负担会直接减半。

---

## 4. 坑① kapt vs KSP：本工程为什么别无选择

**背景**：Hilt 靠注解处理器生成代码。历史上 Kotlin 用 **kapt**（把 Kotlin 编译成 Java 桩再跑 Java 注解处理器，慢且是妥协产物）；新方案 **KSP** 直接理解 Kotlin 符号，构建速度约 2 倍。

**硬约束（2026 年现状，已联网核实）**：
- KSP1 与 kapt 路线**不兼容 Kotlin 2.3+ 和 AGP 9+**——本工程 Kotlin 2.3.20 + AGP 9.2.1，**只能用 KSP2**
- KSP2 起版本与 Kotlin 解耦：不再是老的 `2.0.21-1.0.28`（Kotlin版本-KSP版本）对表格式，现在就是独立版本号 `2.3.9`，一个版本通用多个 Kotlin 版本。**网上 2024 年前的教程还在教你对表，已过时**
- Hilt 侧：**2.59 起 Hilt Gradle 插件才支持 AGP 9**（同时要求 Gradle 9.1+）。如果你在别的项目看到 Hilt 2.5x 早期版本 + AGP 9 报错，先检查这个

**接线要点**（对照 `app/build.gradle.kts`）：插件加 `ksp` + `hilt` 两个；依赖里 `implementation(hilt-android)` 是运行时库，`ksp(hilt-android-compiler)` 是编译期生成器——**一个都不能少**，少了哪个见下面坑②第三条。

## 5. 坑② Hilt 报错排查套路（编译期报错，都有迹可循）

**心法**：Hilt 的报错都发生在【编译期】，报错信息里必然写着"哪条依赖链断了"。从报错的最底部往上读，找到第一个你认识的类名。

**高频①：`[Dagger/MissingBinding] XXX cannot be provided without ...`**
依赖图里没人会造 XXX。检查：它是你的类吗？→ 构造函数忘标 `@Inject`。是三方/系统类吗？→ 忘在 Module 里写 `@Provides`。都写了？→ 检查 `@InstallIn` 的作用域是不是错了（装在子作用域，上层要不到）。

**高频②：运行时崩 `Cannot create an instance of class ... ViewModel`（或编译期提示缺 @AndroidEntryPoint）**
宿主 Activity/Fragment 忘标 `@AndroidEntryPoint`。这是新手第一大坑——ViewModel 侧全对，接入点没声明。

**高频③：`The Hilt Android Gradle plugin is applied but no com.google.dagger:hilt-android-compiler dependency was found`**
加了 `hilt` 插件、忘加 `ksp(libs.hilt.android.compiler)` 依赖（或写成了 implementation）。

**高频④：Sync 直接失败提示 AGP/Gradle 版本**
Hilt < 2.59 遇上 AGP 9 的组合问题，或 Gradle < 9.1。对照本课 §4 的版本约束。

---

### 5.1 实际踩坑：value class 导致 KSP 非法方法名

报错原文：`[ksp] java.lang.IllegalArgumentException: not a valid name: provideAppVersionName-Nwjfrp0`。

本课最初将版本号包装为 `@JvmInline value class AppVersionName`。Kotlin 会对涉及值类的 JVM 方法进行名称改编（mangling），加上哈希后缀；当前 Hilt/KSP 代码生成链路无法接受这里带连字符的方法名。这发生在编译期，App 尚未启动。

修复：在 `di/AppModule.kt` 删除 `@JvmInline`，将声明改为：

```kotlin
data class AppVersionName(val value: String)
```

这样保留独立的依赖类型，`@Provides` 和 Repository 的构造注入无需改动。普通 data class 是 JVM 包装对象，并非 Swift struct；这里选择它是为了代码生成互操作性。仅改提供方法的 Kotlin 名称不能消除值类改编机制。

验证时先运行 `./gradlew :app:kspDebugKotlin`，再运行 `./gradlew :app:assembleDebug`；KSP 成功不代表完整 APK 已构建成功。机制参考：[Kotlin 值类与名称改编](https://kotlinlang.org/docs/inline-classes.html#mangling)。

### 5.2 完整构建暴露的 SDK 版本约束

修复 KSP 后，`:app:checkDebugAarMetadata` 报 AndroidX Hilt 1.4.0 及其引入的 Lifecycle 2.11.0 要求 compileSdk 37，而本工程使用 36.1。现将 AndroidX Hilt 调整为 1.3.0，Dagger Hilt 2.60.1 保持不变；两者属于不同制品，版本号不需要一致。

同时在 Version Catalog 显式声明 Activity Compose 1.8.2、Navigation Compose 2.9.0、Lifecycle 2.9.0。纠正此前注释：这些库不由 Compose BOM 管理；Gradle 仍可能因传递依赖选取更高版本，需要结合依赖图和 AAR 元数据检查。

参考：[AndroidX Hilt 发布说明](https://developer.android.com/jetpack/androidx/releases/hilt)、[Compose BOM 说明](https://developer.android.com/develop/ui/compose/bom)。

## 6. 导航：Tab 切换三件套

`MainScreen.kt` 里 `navController.navigate(route) { ... }` 的三行配置是企业项目的标准咒语，值得背下来：

| 配置 | 作用 | 不写会怎样 |
|---|---|---|
| `popUpTo(起始页) { saveState = true }` | 切 Tab 前把当前栈收起来并保存状态 | Tab 间反复切换叠出深栈，返回键要按 N 次才退出 |
| `launchSingleTop = true` | 连点同一 Tab 不重复创建页面 | 点两下首页，栈里有两个首页 |
| `restoreState = true` | 回到之前的 Tab 时恢复浏览位置 | 每次切回 Tab 都回到顶部、丢失滚动位置 |

**iOS 对照**：SwiftUI `TabView` 天然是"每个 Tab 一个独立栈"，这些行为是白送的；Android 的 navigation-compose 是"单栈 + 路由"模型，多 Tab 体验要靠这三件套显式配置出来。这是两平台导航心智模型最大的差异点。

另外注意 `WanDestination` 枚举集中定义路由——路由字符串只在这一个文件出现，页面增删改都动这里，杜绝魔法字符串散落（S2 加详情页、S4 加登录页都会扩展它）。

---

## 7. 验收 checklist（逐项打勾）

- [x] Sync 成功（首次会下载 Hilt/KSP 依赖，若慢参考 S0 文档坑②配镜像）
- [x] 真机运行：底部三 Tab（首页/广场/我的）可点击切换，选中态高亮为品牌蓝
- [x] 首页显示 "WanReader v0.1.0 · Hilt 注入链路已打通"（版本号是运行时从 PackageManager 读的，证明 Module → Repository → ViewModel → UI 全链路真实工作）
- [x] 系统切深色模式：主题跟随，且主色调是品牌蓝（对比 S0 的默认紫色系）
- [x] 到 `app/build/generated/` 下找到一个 Hilt 生成的 Factory 类，看一眼（§3.4）
- [x] 自测：能说出 `@Inject constructor` 和 `@Provides` 分别什么时候用（§3.2 ③④）

验收通过 → 会话里告诉我 → 标记 S1 ✅ → 进入 S2（网络层：Retrofit + OkHttp + kotlinx.serialization + BaseResponse 统一封装，App 第一次拉到玩Android 真实数据）。
遇到报错 → 完整报错贴回会话，先按 §5 套路自查一遍再看我的排查过程，对照你卡在哪一步。
