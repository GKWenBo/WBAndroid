# Demo 02 · Kotlin 基础 + 协程（Week 1）

> 目标：作为 iOS 工程师，用最短路径把 Kotlin 语法和协程跑通。
> 本 Demo 是一个**可直接在 Android Studio 打开运行**的空壳 App，启动后会执行一组 Kotlin 练习并把结果打到 Logcat。

## 怎么运行
1. Android Studio 打开 `Demo/demo_02`（或根目录用 `File > Open`）。
2. 连接模拟器/真机，点 Run。
3. 打开 **Logcat**，过滤标签 `KotlinDemo`，即可看到 7 段基础练习 + 协程并发结果。

## 本 Demo 涵盖
- [x] `val`/`var`、默认参数、命名参数（≈ Swift let/var/默认参数）
- [x] `data class`（≈ Swift struct，自动 equals/copy）
- [x] `sealed interface` + `when`（≈ Swift enum + 关联值，做 UI 状态机）
- [x] 扩展函数（≈ Swift extension）
- [x] 空安全 `?` / `?:` / `!!`（≈ Swift `?`/`!`）
- [x] 作用域函数 `let`/`run`/`apply`/`with`
- [x] 高阶函数 + 尾随 lambda
- [x] 协程：`suspend`/`async`/`await` 并发合并（**Week1 检查点**）

## iOS 工程师易踩的坑
1. **没有 `;`**：Kotlin 行尾不写分号（写了也不报错，但别写）。
2. **`when` 必须穷尽**：`sealed` 分支没覆盖全编译器直接报错（比 Swift 的 `switch` 更严格）。
3. **`!!` 等于埋雷**：尽量少用，优先 `?.` + `?:`。
4. **`object` 是单例**：不是实例，全局唯一，别当成普通类 new。
5. **协程不是线程**：`suspend` 函数只能在协程/另一个 suspend 里调用；`Dispatchers.Main` 需要 `kotlinx-coroutines-android`（本 Demo 用 `Dispatchers.Default` 规避）。
6. **`CoroutineScope` 要取消**：Activity 销毁时 `scope.cancel()`，否则协程泄漏（≈ 忘了 cancel Task）。
7. **Gradle 同步慢**：国内用 `doc/工程配置.md` 里的腾讯云/阿里云镜像；改了 `libs.versions.toml` 后点 "Sync Project"。
8. **Kotlin 插件不要显式声明**：本机 AGP 9.2.1 会自动注入并应用 Kotlin 插件（这也是 `demo_01` 没声明却能编译 `.kt` 的原因）。在模块 `build.gradle.kts` 里**不要**写 `id("org.jetbrains.kotlin.android")`，否则会因重复应用报 `Cannot add extension with name 'kotlin'`。协程等只需作为普通库依赖（`libs.androidx.coroutines`）引入即可。

## 下一步
理解后可进入 [../demo_03_compose_ui](../demo_03_compose_ui)（Compose UI）。
