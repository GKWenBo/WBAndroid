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
- 验证命令：`./gradlew :app:assembleDebug`；发布级：`./gradlew :app:build`（含 lint + 单测 + release）。
- 运行验证：模拟器 `emulator-5554`（Pixel_10_Pro / API 37.1）。
