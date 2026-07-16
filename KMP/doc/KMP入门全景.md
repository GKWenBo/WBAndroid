# Kotlin Multiplatform (KMP) 全景介绍 —— 写给 iOS 高级开发者

> 定位：这是一份"概念地图 + 桥接手册"，不是照抄官方文档。所有新概念都从你熟悉的 Swift/iOS 出发。
> 时效：内容对齐 2026 年年中生态状态（KMP 生态近两年变化很快，具体版本号请以官方为准）。

---

## 0. 一句话心智模型（最重要）

**KMP 默认共享的是"业务逻辑"，不是 UI。**

把你现在 iOS 工程里的这几层想象一下：

```
┌─────────────────────────────┐
│  SwiftUI / UIKit  (View)     │  ← 各平台自己写
├─────────────────────────────┤
│  ViewModel / Presenter       │  ┐
│  Repository / UseCase        │  │
│  NetworkService / API        │  ├─ 这些用 Kotlin 写一遍，
│  Model / DTO                 │  │   Android 和 iOS 共用
│  数据库 / 缓存                │  ┘
└─────────────────────────────┘
```

KMP 就是：**把 View 层以下的东西抽成一个 Kotlin 模块，编译成 Android 的 `.aar` 和 iOS 的 `.framework`，两端各自的 UI 层去调用它。**

这跟 Flutter / React Native 的哲学**完全不同**：

| | 共享层 | UI | 渲染方式 | 对 iOS 老手的感受 |
|---|---|---|---|---|
| **Flutter** | 几乎全部 | Dart 自绘 | 自带渲染引擎（Skia） | UI 不是原生控件，交互细节要额外调 |
| **React Native** | 大部分 | JS 描述 → 原生控件 | 桥接原生 | JS 桥有性能/调试成本 |
| **KMP（默认玩法）** | 只共享逻辑 | **各平台原生** | 原生 | **UI 完全是你熟悉的 SwiftUI**，逻辑复用 |

> 核心结论：KMP 是"渐进式"的。你可以只共享一个网络层，也可以共享到 ViewModel，甚至（可选）共享 UI。**共享多少完全由你决定**，这是它区别于其它跨平台方案的最大特点。

---

## 1. 为什么 KMP 特别适合你

- **你是双端背景**：懂 Swift + 正在学 Kotlin。JetBrains 的调查显示，多数 KMP 新采用者其实是"只懂 Android 的团队横向扩到 iOS"——他们要现学 iOS 的坑。而你反过来，iOS 侧的集成、Swift 互操作、Xcode 工程这些对多数 Android 人是黑盒的部分，恰恰是你的主场。
- **生态已经成熟**：KMP 核心自 2023 年 11 月起就是 Stable，Google 官方推荐用它在 Android/iOS 间共享业务逻辑。Netflix、McDonald's、Cash App、Philips 等已在生产环境跑了多年。
- **不是"要不要重写"的赌注**：可以从现有 App 里抽一个小模块开始，风险极低。

---

## 2. 共享什么 / 不共享什么（务实版）

**推荐共享（收益高、风险低）：**

- 网络请求、API 层
- 数据模型 / DTO / 序列化
- 数据库、缓存、本地存储
- 业务逻辑、UseCase、校验规则
- ViewModel（现在 Google 的 `ViewModel` 已支持 KMP）
- 依赖注入配置

**谨慎共享（技术已就绪，但要评估）：**

- UI 层（Compose Multiplatform，见 §7）——**新手强烈建议先别碰**

**不共享（本来就该各平台原生）：**

- 平台特定 API：推送、生物识别、相机、HealthKit、权限弹窗等
- 这些用 §4 的 `expect/actual` 机制"声明共享、实现分离"

> 业界公认的起步姿势：**shared logic + native UI**。先证明"共享逻辑"这套在你团队里跑得通，再考虑要不要共享 UI。永远不要从共享 UI 开始。

---

## 3. 工程结构与"源集"（source sets）

KMP 项目的核心是 **源集（source set）**，本质是"按平台划分的代码目录"：

```
shared/                        ← 共享模块（会被两端引用）
  src/
    commonMain/     ← 公共代码，两端都能用（不能碰平台 API）
    androidMain/    ← 只在 Android 编译，可用 Android SDK
    iosMain/        ← 只在 iOS 编译，可用 Kotlin/Native + Apple API
    commonTest/     ← 公共测试
```

**Swift 类比**：你在 Swift 里写平台差异，用的是编译标志：

```swift
#if os(iOS)
    // ...
#elseif os(macOS)
    // ...
#endif
```

KMP 把这种"平台分支"从**代码内的宏**升级成了**目录级的结构**。`commonMain` 里写通用逻辑；平台差异不是靠 `if`，而是靠"在不同目录放不同实现"。这带来一个关键机制——

---

## 4. `expect` / `actual`（务必吃透）

这是 KMP 处理平台差异的核心语法。

- `expect`：在 `commonMain` 里**声明**"我需要一个这样的东西，但先不实现"
- `actual`：在 `androidMain` / `iosMain` 里分别**提供实现**

```kotlin
// commonMain —— 声明契约
expect fun platformName(): String

expect class DeviceInfo() {
    fun osVersion(): String
}
```

```kotlin
// androidMain —— Android 实现
actual fun platformName(): String = "Android"

actual class DeviceInfo actual constructor() {
    actual fun osVersion(): String = android.os.Build.VERSION.RELEASE
}
```

```kotlin
// iosMain —— iOS 实现（可直接调 Apple API！）
import platform.UIKit.UIDevice

actual fun platformName(): String = "iOS"

actual class DeviceInfo actual constructor() {
    actual fun osVersion(): String =
        UIDevice.currentDevice.systemVersion
}
```

**Swift 类比的三个层次（从粗到准）：**

1. **粗略类比**：像定义一个 `protocol`，然后各平台写 `extension` 实现。
2. **更准的类比**：像 C/OC 的"头文件声明 + 平台各自 .m 实现"——`expect` 是接口契约，编译期强制两端都要有 `actual`，否则编译不过。
3. **关键区别**：`expect/actual` 是**编译期绑定**、**零运行时开销**。不是运行时多态，编译后 `commonMain` 里的调用会直接链到对应平台的 `actual`。

> 注意 `commonMain` 里**不能**直接写 `import UIKit` 或 `import android.*`。凡是碰平台 API 的东西，都得通过 `expect/actual`（或接口 + 平台注入）隔离出去。这跟你写 Swift Package 时把平台相关代码隔离到独立 target 的思路一致。

---

## 5. 关键库生态（2026）—— iOS 对照表

KMP 之所以能用，是因为有一整套"纯 Kotlin、跨平台"的库。下面是你会天天打交道的，附 iOS 对照：

| 用途 | KMP 库 | 你在 iOS 用的 | 备注 |
|---|---|---|---|
| 网络 | **Ktor** | URLSession / Alamofire | Ktor 在 iOS 底层走的就是 URLSession |
| JSON 序列化 | **kotlinx.serialization** | `Codable` | 概念几乎一一对应，`@Serializable` ≈ `Codable` |
| 数据库 | **SQLDelight** 或 **Room（KMP 版）** | Core Data / GRDB | Room 2025 年起支持 KMP；SQLDelight 更"SQL 优先" |
| 键值存储 | **DataStore（KMP 版）** | UserDefaults | Google 官方，已支持 KMP |
| 依赖注入 | **Koin** | Swinject / 手写 | Koin 纯 Kotlin，跨平台 |
| 异步 | **coroutines / Flow** | async-await / AsyncSequence / Combine | 见 §6，这是互操作的重点 |
| 状态管理 | **StateFlow / SharedFlow** | `@Published` / `CurrentValueSubject` | `StateFlow` ≈ `CurrentValueSubject`（有初值） |
| 日期时间 | **kotlinx-datetime** | `Date` / `Calendar` | |
| 图片加载 | **Coil 3** | Kingfisher / SDWebImage | Coil 3 支持 Compose Multiplatform |

> 一个让你安心的信号：Google 把自家 Jetpack 的核心库（Room、DataStore、ViewModel、Paging、Navigation）陆续都做了 KMP 支持。当官方一方库都在用 KMP，说明这是长期投入，不是短期噱头。

---

## 6. iOS 侧集成 —— 你最关心、也最容易踩坑的部分

### 6.1 Kotlin 代码是怎么变成 iOS 能用的东西的？

```
Kotlin 共享代码
   │  Kotlin/Native 编译
   ▼
Objective-C 头文件 + framework    ← 这是"翻译中转层"
   │  Swift 自动桥接
   ▼
你在 Swift 里 import shared 直接调用
```

问题就出在中间那层 **Objective-C**。Kotlin 的很多现代特性 Obj-C 没有，翻译时会"降级失真"：

| Kotlin 特性 | 裸桥接到 Swift 后 | 痛点 |
|---|---|---|
| `suspend` 函数 | 变成 completion handler 回调 | 丢了结构化并发、取消传播 |
| `Flow<T>` | 变成泛型丢失的对象 | 拿到的是 `Any?`，还不能取消 |
| `sealed class` | 变成普通类继承层级 | `switch` 不能穷尽，被迫写 `default` |
| 泛型 | 大量退化成 `Any` | 类型安全丢失 |
| `Int?` 等可空基础类型 | 装箱成 `KotlinInt` | Swift 侧代码啰嗦 |

### 6.2 解决方案：SKIE（当前主力，几乎必装）

**SKIE**（Touchlab 出品，读作 "sky"）是一个 Kotlin 编译器插件，它在 Obj-C 头文件之上**再生成一层 Swift 友好的代码**，把上面丢失的特性补回来：

- `suspend fun` → 真正的 Swift `async throws` 函数，**支持双向取消**
- `Flow<T>` → Swift `AsyncSequence`，**泛型 `T` 保留**，`for await` 直接用
- `sealed class` → 真正的 Swift `enum`，`switch` 可穷尽、可取关联值
- Kotlin `enum` → Swift `enum`
- 默认参数 → 生成 Swift 重载，不用每个参数都传

**效果对比**——同一个共享仓库：

```kotlin
// commonMain (Kotlin)
class UserService(private val api: UserApi) {
    fun observeUser(id: String): Flow<User> = api.observeUser(id)
    suspend fun refreshUser(id: String) = api.refreshUser(id)
}
```

装了 SKIE 之后，在 Swift 里就像调原生 async 代码：

```swift
// iOS 侧 (Swift) —— 感觉不到这是 Kotlin
@MainActor
class ProfileViewModel: ObservableObject {
    @Published private(set) var user: User?
    private let service: UserService

    func startObserving() {
        Task {
            for await next in service.observeUser(id: "123") {  // Flow → AsyncSequence
                self.user = next
            }
        }  // Task 结束自动取消，无需手动持有 Job
    }

    func refresh() async {
        try? await service.refreshUser(id: "123")               // suspend → async
    }
}
```

> `@Throws` 提醒：SKIE 的 suspend 为了支持取消会翻成 throwable async。如果 Kotlin 侧会抛出非取消异常，需要在 Kotlin 函数上加 `@Throws(...)`，异常才能在 Swift 侧被 `catch`，否则会直接 crash。

**同类工具 KMP-NativeCoroutines**：解决同样的问题，靠 `@NativeCoroutines` 注解 + Swift 侧 `asyncSequence(for:)`。SKIE 更"自动"、侵入更小；两者**不要同时启用**，会冲突。新项目一般直接选 SKIE。

### 6.3 未来方向：Swift Export（跳过 Obj-C）

Kotlin 2.2.20 起引入了**实验性的 Swift Export**：Kotlin 直接生成 Swift，不再经过 Obj-C 中转，从根上解决失真问题。JetBrains 的目标是 **2026 年发布覆盖主要场景的 stable 版本**。

现状（务必知道，避免踩坑）：

- 还是**实验性**，API 会变
- 目前只支持 Xcode 直接集成，暂不支持 CocoaPods/Carthage
- **SKIE 目前还不支持 Swift Export**

**给你的实操建议**：2026 年新项目，**先用 SKIE**（成熟、稳），把 Swift Export 当作"密切关注、还别押上生产"的东西。等它 stable 且 SKIE 或等价工具跟进后再迁移。

---

## 7. Compose Multiplatform（可选的 UI 共享层）

如果你想连 UI 都共享，这条路现在**技术上可行**了：

- Compose Multiplatform for iOS 在 **2025 年 5 月随 1.8 版本达到 Stable**，之后持续演进（当前已到 1.11+）。
- 底层用 Skia + Metal 自绘，所以一个按钮在 Pixel 和 iPhone 上"逐像素一致"。
- 已有真实的 iOS 滚动物理、原生文本选择、无障碍接线、拖拽等。

**但对你（iOS 老手）要泼的冷水——它不是 SwiftUI：**

- 默认是 **Material Design 风格**，没有原生 Cupertino 控件套件，iOS 观感需要额外打磨
- 与 SwiftUI/UIKit 的互操作有 workaround，但不是一等公民
- 无障碍（VoiceOver / XCTest）暴露不完整
- Xcode 无法可视化调试 Compose 视图
- 二进制体积会有个位数 MB 的额外开销

**务实结论**：主消费级界面继续用 SwiftUI；内部工具、仪表盘、配置页这类"功能优先、观感其次"的界面，可以考虑 Compose Multiplatform 共享。对你的学习路线，**这是很后面的事**。

---

## 8. 典型分层架构（推荐蓝图）

```
        Android App                     iOS App
   ┌──────────────────┐          ┌──────────────────┐
   │  Jetpack Compose  │          │     SwiftUI       │   ← 各平台原生 UI
   └────────┬─────────┘          └────────┬─────────┘
            │                              │
            │       ┌──────────────┐       │
            └──────▶│   ViewModel   │◀──────┘        ← 可共享（KMP ViewModel）
                    ├──────────────┤
                    │  UseCase /    │
                    │  Repository   │                 ← shared / commonMain
                    ├──────────────┤
                    │ Ktor  │ Room  │
                    │ Serialization │
                    └──────────────┘
                    iOS 侧经 SKIE 消费
```

---

## 9. 学习路线建议（结合你当前进度）

你现在正处在"Android native 基础"阶段（Compose / ViewModel / Room / Flow / Hilt）。**这是对的，别急着跳 KMP。** 原因：KMP 共享层用的正是这些同一套东西（coroutines、Flow、StateFlow、Room、DI），你 Android 基础越扎实，切 KMP 越顺。

**建议的切入时机与顺序：**

1. **先完成**（你 roadmap 里的）：Compose → ViewModel/Room/StateFlow/Hilt。等你能独立写一个带网络 + 本地缓存 + 单向数据流的 Android App。
2. **第一个 KMP 模块，选这些**（逻辑重、无 UI、易验证）：
   - Analytics 埋点封装
   - Auth token 管理
   - 纯网络层（Ktor + serialization）
3. **iOS 侧接入**：给这个 shared 模块配好 SKIE，在你的 Swift 工程里 `import` 调用一次 `suspend` 和一个 `Flow`，亲手体验 §6 的桥接。
4. **再往上抽**：Repository → ViewModel。
5. **UI 共享**（Compose Multiplatform）留到最后，甚至可以永远不做。

> 一个高杠杆的迁移策略：**别新建空项目练**，而是从你现有的某个 iOS App 里挑一个独立、逻辑清晰的功能（比如"配置同步"或"埋点"），把它的逻辑用 KMP 重写、双端接入。真实场景才会逼出真实的坑。

---

## 10. 自测题（合上文档，口头答）

1. 用一句话说清：KMP 和 Flutter 在"共享什么"上的根本区别是什么？
2. `commonMain` 里为什么不能直接 `import UIKit`？该怎么用平台 API？
3. `expect/actual` 是运行时多态还是编译期绑定？它更像 Swift 里的哪个东西，又有什么关键不同？
4. 不装 SKIE 时，Kotlin 的 `suspend` 函数和 `Flow<T>` 在 Swift 侧分别退化成什么？各丢了什么能力？
5. SKIE 把 `sealed class` 翻成 Swift 的什么？这对 `switch` 有什么好处？
6. Kotlin 的 `StateFlow` 最接近你 iOS 里的哪个类型？为什么不是 `PassthroughSubject`？
7. 为什么业界建议"从共享逻辑起步，永远别从共享 UI 起步"？Compose Multiplatform 已经 stable 了，这个建议为什么依然成立？
8. Swift Export 想解决什么问题？为什么 2026 年的新项目还是先选 SKIE？

## 11. 动手练习（等你 Android 基础到位后做）

- **练习 A**：新建一个 KMP 共享模块，用 `expect/actual` 实现一个 `platformName()` + `appVersion()`，Android 返回 `BuildConfig`，iOS 返回 `Bundle`。目标：跑通目录级平台分支 + 双端调用。
- **练习 B**：在 commonMain 用 Ktor + kotlinx.serialization 写一个拉取公开 API（如 GitHub 用户信息）的 `UserRepository`，暴露一个 `suspend fun fetchUser(): User` 和一个 `Flow<LoadState>`。
- **练习 C**：给练习 B 配上 SKIE，在一个最小 SwiftUI 工程里用 `async/await` 调 `fetchUser()`、用 `for await` 消费那个 `Flow`。**体会"感觉不到这是 Kotlin"的那一刻**——这就是 KMP 对 iOS 开发者的核心价值。

---

## 附：一句话记忆锚点

> **KMP = 把 App 的"下半身"（逻辑/数据）用 Kotlin 写一次，两端原生 UI 各自套上去；iOS 侧靠 SKIE 把 Kotlin 的 suspend/Flow/sealed 翻译成地道的 async/AsyncSequence/enum。**
