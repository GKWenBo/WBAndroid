# KMP 进阶：`expect` / `actual` 实战模式手册

> 承接《KMP 入门全景》§4。这份聚焦"真实项目里到底怎么用 `expect/actual`"，包含 5 种核心模式、选型决策、易错点，以及一个你必须先知道的稳定性事实。
> 时效对齐 2026 年年中（Kotlin 2.3 / 2.4 线）。

---

## 0. 先记住一个决定一切的事实（稳定性）

`expect/actual` 不是"铁板一块"，它的稳定性**取决于你把它用在什么上**：

| 用在……上 | 稳定性 | 编译器行为 |
|---|---|---|
| **函数**（`expect fun`） | ✅ **Stable** | 无警告，放心用 |
| **属性**（`expect val` / `var`） | ✅ **Stable** | 无警告，放心用 |
| **类 / 接口 / 对象 / 枚举 / 注解 / `actual typealias`** | ⚠️ **Beta** | 会报警告，需手动抑制 |

Beta 警告长这样，需要在 Gradle 里加编译器参数抑制：

```kotlin
// build.gradle.kts
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
```

**这条事实的实战含义**（非常重要）：

> 凡是"有行为的平台服务"（Logger、存储、Analytics、网络引擎……），**优先用"接口 + 依赖注入"，而不是 `expect class`**。官方文档也是这个态度：用普通 Kotlin 接口在 common 里表达依赖，靠 DI 注入平台实现，`expect/actual` 只需要出现在 DI 的配置处。
>
> `expect class` 留给少数确实划算的场景（见 §5）。

**Swift 类比**：这就像你在 iOS 里，能用 `protocol` + 依赖注入解决的，就不要用条件编译 `#if os()` 去写两套具体类型——前者可测、可 mock、耦合低。KMP 里"接口 + DI vs `expect class`"是同一个权衡。

---

## 1. `expect/actual` 能落在哪些语法构造上（全景）

```
expect fun            ← 平台函数        ✅ Stable   最常用
expect val / var      ← 平台常量/属性    ✅ Stable
expect class          ← 平台类          ⚠️ Beta
expect object         ← 平台单例        ⚠️ Beta
expect interface      ← 平台接口        ⚠️ Beta   (少见)
expect annotation class ← 平台注解      ⚠️ Beta   (配 @OptionalExpectation)
actual typealias      ← 别名到已有平台类型 ⚠️ Beta   威力大
```

下面按"实战推荐度"逐个讲模式。

---

## 2. 模式一：`expect fun` 工厂函数（首选，最常用）

需要一个平台特定的**单一能力**时，用顶层 `expect fun`。它是 Stable 的，最简单。

```kotlin
// commonMain
expect fun currentTimeMillis(): Long
expect fun randomUuidString(): String
expect fun openUrl(url: String)
```

```kotlin
// androidMain
actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun randomUuidString(): String = java.util.UUID.randomUUID().toString()
actual fun openUrl(url: String) {
    // 通过注入的 Context 启动 Intent（真实项目里 Context 靠 DI 传入）
}
```

```kotlin
// iosMain
import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.Foundation.NSURL

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
actual fun randomUuidString(): String = NSUUID().UUIDString()
actual fun openUrl(url: String) {
    val nsUrl = NSURL(string = url)
    UIApplication.sharedApplication.openURL(nsUrl)
}
```

**Swift 类比**：等价于你在 Swift 里写一个自由函数，用 `#if os(iOS)` 分平台实现——但 KMP 把分支拆到了不同目录，且编译期强制两端都实现。

> 什么时候够用：能力是"无状态、单次调用"的（取当前时间、生成 UUID、打开链接）。一旦要"持有状态 / 多个方法协作"，就升级到模式二。

---

## 3. 模式二：接口 + 平台实现 + DI（有行为的服务，强烈推荐）

这是**生产项目里最该用的模式**，专治"平台服务"：Logger、KV 存储、Analytics、生物识别、推送等。

**第 1 步：common 里只声明接口（纯 Kotlin，Stable，无 Beta 警告）**

```kotlin
// commonMain
interface KeyValueStore {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
}
```

**第 2 步：各平台写实现**

```kotlin
// androidMain
class AndroidKeyValueStore(
    private val prefs: android.content.SharedPreferences
) : KeyValueStore {
    override fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()
    override fun getString(key: String) = prefs.getString(key, null)
    override fun remove(key: String) = prefs.edit().remove(key).apply()
}
```

```kotlin
// iosMain
import platform.Foundation.NSUserDefaults

class IosKeyValueStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : KeyValueStore {
    override fun putString(key: String, value: String) =
        defaults.setObject(value, forKey = key)
    override fun getString(key: String) =
        defaults.stringForKey(key)
    override fun remove(key: String) =
        defaults.removeObjectForKey(key)
}
```

**第 3 步：只在这里用一点 `expect/actual`——注入平台实现**

有两种接法：

*(a) 用 `expect fun` 工厂（保持 Stable，无 Beta 警告，推荐）*

```kotlin
// commonMain
expect fun provideKeyValueStore(): KeyValueStore

// androidMain
actual fun provideKeyValueStore(): KeyValueStore =
    AndroidKeyValueStore(/* prefs from DI */)

// iosMain
actual fun provideKeyValueStore(): KeyValueStore = IosKeyValueStore()
```

*(b) 或者用 Koin 等 DI 框架，`expect/actual` 只出现在模块声明里*

```kotlin
// commonMain
expect val platformModule: org.koin.core.module.Module

// androidMain
actual val platformModule = module { single<KeyValueStore> { AndroidKeyValueStore(get()) } }

// iosMain
actual val platformModule = module { single<KeyValueStore> { IosKeyValueStore() } }
```

**Swift 类比**：`protocol KeyValueStore` + `UserDefaultsStore: KeyValueStore`，然后在组装根注入。你的 common 业务代码只依赖接口，完全不知道底下是 SharedPreferences 还是 NSUserDefaults——和你在 iOS 里做 protocol 抽象 + 构造注入是同一套心法。

> 为什么优先它而不是 `expect class KeyValueStore`：接口是 Stable、可 mock（测试时注入假实现）、耦合更低，且避开了 §0 的 Beta 警告。**记住：有行为 → 接口 + DI。**

---

## 4. 模式三：`actual typealias` —— 别名到已有平台类型

有时某个平台**已经存在**一个正好合适的类型，你不想重写，只想"把它认领为共享类型"。这时用 `actual typealias`。

```kotlin
// commonMain
expect class PlatformFile {
    fun readText(): String
    val path: String
}
```

```kotlin
// androidMain —— 直接别名到 java.io.File？
// 注意：typealias 目标类型必须满足 expect 声明的全部成员，
// 若签名对不上，通常还是得包一层。这里演示能对上的简化场景：
actual typealias PlatformFile = MyAndroidFileWrapper
```

更有说服力的真实用例是**注解**（见 §7）和**并发原语**。例如把某个平台已有的锁类型认领过来：

```kotlin
// commonMain
expect class Lock() {
    fun lock()
    fun unlock()
}
```

**关键规则**：`actual typealias` 指向的类型，必须**逐成员匹配** `expect` 声明（方法名、签名、属性都要对得上）。对不上就退回"写一个实现类"。

**注意**：`actual typealias` 属于"类家族"，同样触发 §0 的 Beta 警告。

**Swift 类比**：就像你写 `typealias UUID = Foundation.UUID`，把系统类型收编成你模块 API 的一部分——省得自己造轮子。

---

## 5. 模式四：`expect class` / `expect object`（少数划算场景）

虽然是 Beta，但有几种情况 `expect class` / `expect object` 确实比接口+DI 更顺手：

**(a) 极薄的平台信息载体**（无需 mock、无需多实现）

```kotlin
// commonMain
expect class Platform() {
    val name: String
    val osVersion: String
}

// androidMain
actual class Platform actual constructor() {
    actual val name: String = "Android"
    actual val osVersion: String get() = android.os.Build.VERSION.RELEASE
}

// iosMain
import platform.UIKit.UIDevice
actual class Platform actual constructor() {
    actual val name: String = "iOS"
    actual val osVersion: String
        get() = UIDevice.currentDevice.systemVersion
}
```

> 注意语法细节：`expect` 声明里写了主构造函数 `Platform()`，`actual` 端就要写 `actual class Platform actual constructor()`。

**(b) 平台单例用 `expect object`**

```kotlin
// commonMain
expect object PlatformDispatchers {
    val io: kotlinx.coroutines.CoroutineDispatcher
}
```

> 现实提示：`Dispatchers.IO`、日期时间、原子操作、UUID 这些"看似要 `expect/actual`"的东西，**多半已有跨平台库直接解决，别自己造**：
> - UUID → `kotlin.uuid.Uuid`（标准库，已稳定）
> - 日期时间 → `kotlinx-datetime`
> - 原子操作 → `kotlin.concurrent.atomics` / AtomicFU
> - 调度器 → `kotlinx-coroutines` 自带 `Dispatchers.Default/Main/IO`
> - KV 存储 → `multiplatform-settings` / DataStore
> - 日志 → Napier / Kermit
>
> 动手写 `expect/actual` 之前，先问一句"这个是不是已经有库了"。

---

## 6. 中间源集：在"最高的共享层"写 `actual`（避免重复）

一个 iOS target 其实展开成多个叶子源集：`iosArm64Main`（真机）、`iosSimulatorArm64Main`（模拟器）、`iosX64Main`。如果每个都写一遍 `actual`，会重复三份。

正确做法：**在中间源集 `iosMain` 里写一次 `actual`**，编译器会把它用到所有 iOS 叶子平台。同理，若 iOS + macOS 逻辑一致，可以提到 `appleMain` 或 `nativeMain`。

```
commonMain          ← expect
  └─ nativeMain
       └─ appleMain
            └─ iosMain          ← 通常在这里写一次 actual 就够
                 ├─ iosArm64Main
                 └─ iosSimulatorArm64Main
```

**Swift 类比**：类似你在 Swift 里用 `#if os(iOS) || os(macOS)` 合并苹果平台的分支，而不是给每个具体架构各写一遍。

---

## 7. 模式五：`expect annotation class` + `@OptionalExpectation`（平台注解）

经典痛点：Android 有 `@Parcelize`（自动序列化到 `Parcelable`），iOS 根本没这东西。你想在 common 的数据类上打一个注解，Android 生效、iOS 无害。

```kotlin
// commonMain
@OptionalExpectation
expect annotation class CommonParcelize()
```

```kotlin
// androidMain
actual typealias CommonParcelize = kotlinx.parcelize.Parcelize
```

```kotlin
// iosMain —— 什么都不用写！
```

`@OptionalExpectation` 的魔力：**允许某些平台不提供 `actual`**。iOS 侧这个注解直接被忽略，不报错。于是你能在 common 里这样用：

```kotlin
// commonMain
@CommonParcelize
data class User(val id: String, val name: String) // Android 自动 Parcelable，iOS 无害
```

**Swift 类比**：有点像你用 `#if os(iOS)` 包住一个只在某平台存在的属性包装器/宏——差异被隔离，跨平台代码保持干净。这个模式在处理 Android 专有注解（`@Parcelize`、`@Keep` 等）时非常实用。

---

## 8. 选型决策树（贴墙用）

```
我需要一个平台特定的东西……
│
├─ 是"单次调用的能力"（取时间/UUID/打开URL）？
│     → expect fun               ✅ Stable，首选
│
├─ 是"有行为的服务"（存储/日志/Analytics/生物识别）？
│     → 接口 + 平台实现 + DI       ✅ Stable，强烈推荐
│        （expect/actual 只用在工厂函数或 DI 模块）
│
├─ 平台已有正好合适的类型，只想认领？
│     → actual typealias          ⚠️ Beta，但省事
│
├─ 极薄的信息载体 / 平台单例，不需要 mock？
│     → expect class / expect object  ⚠️ Beta，可接受
│
├─ 需要一个平台专有注解（如 @Parcelize）？
│     → @OptionalExpectation + expect annotation class + actual typealias
│
└─ 先等一下——这需求是不是已经有跨平台库了？
      → 有的话直接用库（UUID/datetime/settings/coroutines/日志）
```

---

## 9. 易错点清单（你重视正确性，逐条记）

1. **稳定性分家**：`expect fun` / `expect val` 是 Stable；`expect class/object/interface/enum/annotation` 和 `actual typealias` 是 **Beta**，会警告，需 `-Xexpect-actual-classes` 抑制。
2. **默认参数只能写在 `expect` 侧**：`actual` 端**不能**重复默认值，否则编译报错。
   ```kotlin
   // commonMain — 默认值放这里
   expect fun greet(name: String = "World"): String
   // androidMain — 不要再写 = "World"
   actual fun greet(name: String): String = "Hi, $name"
   ```
3. **`expect` 声明不能有函数体 / 初始化器**：它只是契约。
4. **签名必须逐字匹配**：`actual` 的包名、函数名、参数、返回类型都要与 `expect` 一致；可见性不能比 `expect` 更严。
5. **必须同一个 Gradle 模块**：`expect` 和它的 `actual` 要在**同一模块的不同源集**里，不能跨模块拆分。跨模块共享请改用"接口 + DI"。
6. **在最高共享源集写 `actual`**：别在每个叶子平台源集里重复（见 §6）。
7. **`actual typealias` 目标要满足全部 expect 成员**：对不上就老实写实现类。
8. **别把 `commonMain` 弄脏**：`commonMain` 里禁止 `import android.*` / `import platform.*`。凡碰平台 API 的都要经 `expect/actual` 或接口隔离出去。

---

## 10. 自测题（合上文档口答）

1. `expect fun` 和 `expect class` 的稳定性一样吗？后者编译时会发生什么，怎么处理？
2. 要给 iOS/Android 各写一个"读写 KV 存储"的服务，你会选 `expect class` 还是"接口 + DI"？给出两条理由。
3. 默认参数值 `= "World"` 应该写在 `expect` 侧还是 `actual` 侧？写错会怎样？
4. `actual typealias` 适合什么场景？它有什么前置条件？
5. `@OptionalExpectation` 解决了什么问题？举一个 Android 专有注解的例子说明它怎么用。
6. iOS target 展开成 `iosArm64Main`/`iosSimulatorArm64Main` 等多个叶子源集，`actual` 应该写在哪个源集里，为什么？
7. 下面这个需求，你的第一反应应该是什么？——"我想在 common 里生成 UUID 和取当前时间。"（提示：不一定要 `expect/actual`）

## 11. 动手练习

- **A（模式一）**：用 `expect fun` 实现 `platformName()` + `appVersion()`，Android 读 `BuildConfig`，iOS 读 `NSBundle`。确认能在 `commonMain` 调用、双端编译通过。
- **B（模式二）**：把练习 A 的"存储 App 启动次数"需求，做成 `interface LaunchCounter` + 两端实现（SharedPreferences / NSUserDefaults）+ `expect fun provideLaunchCounter()` 注入。然后写一个 `commonMain` 的单元测试，注入一个**内存假实现**验证计数逻辑——体会"接口 + DI"带来的可测性。
- **C（模式五 vs 二对比）**：先用 `expect class Platform` 写一版设备信息，观察 Beta 警告；再改写成"接口 + DI"版本，对比两者在可测试性上的差异，写下你的结论。

---

## 附：一句话记忆锚点

> **`expect fun`/`val` 放心用；有行为的服务一律"接口 + DI"，`expect/actual` 只在注入点露一次脸；`expect class`/`typealias`/注解是 Beta，用在薄载体和平台注解上；动手前先查有没有现成跨平台库。**
