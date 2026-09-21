# bilibili_demo1 项目约定

- **UI 方案：XML 布局 + ViewBinding**（用户要求从默认 Compose 模板改回 XML）。
  - Activity 继承 `AppCompatActivity`；布局放 `res/layout/`；`buildFeatures { viewBinding = true }`。
  - 不用 Compose，不要重新引入 `org.jetbrains.kotlin.plugin.compose` 与 compose 依赖。
- **构建环境**：AGP 9.2.1 / Gradle 9.4.1 / compileSdk `release(37)`（37.0，已安装）。
  - AGP 9 内置 Kotlin 插件，子模块不要再声明 `org.jetbrains.kotlin.android`，根工程也不要声明 kotlin 插件。
  - AGP 9.2.1 支持的最高 compileSdk 是 37.0，用 37.2 会出未测试警告。
  - core-ktx 1.19.0 必须配 compileSdk ≥ 37。
- 主题：`Theme.AppCompat.DayNight.NoActionBar`（`res/values/themes.xml`）。
  - **所有 Activity 都必须继承 `AppCompatActivity`**：AppCompatDelegate 只在 AppCompatActivity 下生效，
    否则 DayNight 深色模式不跟随，页面间表现不一致。
- 页面跳转用显式 Intent：`Intent(this, XxxActivity::class.java)` + `startActivity(intent)`；
  Kotlin 里**不能**写 `setClass(A, B)`（类名不是表达式，会报 "does not have a companion object"）。
- **统一 Activity 模板**：`private lateinit var binding: XxxBinding` → `enableEdgeToEdge()` →
  `binding = XxxBinding.inflate(layoutInflater)` → `setContentView(binding.root)` →
  `ViewCompat.setOnApplyWindowInsetsListener(binding.root)` 设置 `screen_padding + systemBars`。
  新页面照抄 `MainActivity2` 即可，不要用 `findViewById`。
- 调试注意：Manifest 里 `exported=false` 的 Activity，`adb shell am start`（含 `run-as`）会被
  ActivityManagerService 拒绝，属正常行为；实测需先在 UI 上接入口。
- **特例**：演示 margin/padding 的页面（如 `MarginActivity`）insets 不能直接用 `screen_padding`，
  要先记录 XML 里写死的基础 padding 再叠加 systemBars，否则 XML 的 padding 会被 `setPadding` 覆盖、
  演示内容消失。
- 验证命令：`./gradlew :app:assembleDebug`；发布级：`./gradlew :app:build`（含 lint + 单测 + release）。
- 运行验证：模拟器 `emulator-5554`（Pixel_10_Pro / API 37.1）。

## Git

- **仓库根是 `/Users/wenbo/Desktop/WBAndroid`**，里面还有 `Compose/bi_demo1`、`Demo/demo_05` 等其它练习项目。
  提交时必须限定 pathspec：`git commit -m "..." -- Demo/bilibili_demo1`，否则会把别的项目一起提交进去。
- `local.properties`、`/build`、`.idea/workspace.xml` 等已被 `.gitignore` 排除；`.idea/gradle.xml`、
  `.idea/misc.xml` 等按 Android Studio 默认模板**是纳入版本管理的**，提交时保留即可。
- 根 `.gitignore` 已加 `**/.workbuddy/*.png`（验证截图不入库，但 `.workbuddy/memory/*.md` 要提交）
  和 `**/.kotlin/`（Kotlin 编译会话产物）。提交本项目**无需再手工排除截图**。
- 同仓其它项目的 `.idea` 改动常会一起被 `git add` 进来。提交前先看
  `git status --short` 有没有串项目，再用 pathspec 限定。
  - `Compose/bi_demo1/` 是**空壳目录**（只有 `.idea`，无任何源码），不要提交。
  - `Demo/demo_05` 的 `workspace.xml` 已被它自己的 `.gitignore` 排除，不会进暂存区；
    其余 `.idea/*.xml`（gradle.xml / misc.xml 等）按 Studio 默认模板正常入库。
