# WanReaderKMP · 企业级 Kotlin Multiplatform 教学课程 · 设计文档

> 日期：2026-07-13
> 状态：已与学员逐节确认通过
> 学员背景：资深 iOS 工程师（Swift/SwiftUI/MVVM/Combine），已系统学习 Kotlin，正在 WanReader（纯 Android）教学线学习中

## 1. 目标与定位

通过一个完整的双端项目 **WanReaderKMP**，带学员掌握企业主流形态的 Kotlin Multiplatform 开发：

1. KMP 在企业的真实落地方式：**共享业务逻辑 + 双端原生 UI**（SwiftUI / Compose），不共享 UI
2. 每课配详细教学文档（iOS 视角对照 + 坑点专节 + 验收 checklist），全中文
3. 重点讲透实际开发中的坑，尤其是 iOS 开发者最关心的 Swift 互操作与存量工程接入
4. 一课一课推进，全程记录学习进度

**课程代号 K 线（K0～K9，共 10 课）**，独立于现有两条线：

- 根目录 4 周路线（W 线）——不受影响
- WanReader 纯 Android 教学线（S 线）——**K 线前置条件：S3 完成**（架构分层、网络层、首页列表已在纯 Android 中跑通，K 线只需聚焦"多平台差异"本身）

本设计现在定稿，正式开课时间为 S3 验收通过之后。

## 2. 项目定位

- **项目名**：WanReaderKMP
- **位置**：仓库根目录 `WanReaderKMP/`，与 `WanReader/`、`Demo/` 平级
- **数据源**：玩Android（WanAndroid）免费开放 API——与 S 线同一业务，学员已熟悉，可专注 KMP 本身，并与纯 Android 版对照架构差异

### 工程结构

```
WanReaderKMP/
├── shared/          # KMP 共享模块（commonMain / androidMain / iosMain）
├── androidApp/      # Android 壳工程，Compose UI
├── iosApp/          # Xcode 工程，SwiftUI
├── doc/             # 每课教学文档 K0_xxx.md ...
└── 教学进度.md       # K 线单一真源进度文件
```

### V1.0 功能范围（刻意精简，聚焦 KMP 本身）

- 首页文章列表（分页加载更多）
- 登录与会话
- 收藏 / 取消收藏（乐观更新）
- 文章详情（双端各自原生 WebView）

不做 Paging 3（KMP 支持不成熟）——这一取舍本身作为企业选型教学点在 K3 讲解。

## 3. 技术选型（对齐企业主流 KMP 组合）

| 领域 | 选型 | 选型理由（企业视角） |
|---|---|---|
| 网络 | Ktor Client + kotlinx.serialization | KMP 事实标准；Android 用 OkHttp 引擎、iOS 用 Darwin 引擎，双引擎差异是教学点 |
| 数据库 | SQLDelight | 存量企业 KMP 项目主流选择；与 S6 的 Room 对照，Room KMP 版在课内做对比讲解 |
| 键值存储 | multiplatform-settings | 双端映射 SharedPreferences / NSUserDefaults，iOS 开发者秒懂 |
| DI | Koin | Hilt 不支持 KMP；与 S 线 Hilt 形成对照教学点 |
| 共享 ViewModel | androidx.lifecycle ViewModel（KMP 版） | Google 官方路线；课内讲"共享到哪一层"的企业分歧（只共享 UseCase vs 共享 VM） |
| Swift 互操作增强 | SKIE（Touchlab） | 企业标配：suspend→async/await、Flow→AsyncSequence、密封类→enum |
| iOS UI | SwiftUI | 学员母语技术，直接消费共享模块 |
| Android UI | Jetpack Compose | 复用 S 线经验 |
| 版本基线 | Kotlin 2.3.x / AGP 9.2.1 / Gradle 9.4.1 | 复用本机已验证组合，避免重踩环境坑 |

## 4. 教学模式

- **我写你学**：每课由 Claude 产出代码 + 教学文档；学员本地验收（Android Studio Sync + Xcode 跑通），确认后才进下一课
- **坑在现场讲**：配置坑、互操作坑均安排在会真实遇到它的课程现场讲解与排查，排查过程记入文档
- **沙箱验证边界**：共享模块单测、Gradle 配置在沙箱先验证；iOS 端与真机部分由学员本地验收（与 S0 模式一致）

## 5. 课程大纲（方案 A：自底向上分层推进，共 10 课）

| # | 课程 | 核心内容 | 现场讲的坑（举例） | 验收标准 |
|---|---|---|---|---|
| K0 | KMP 全景与双端工程骨架 | 企业落地形态（谁在用、共享到哪层、与 Flutter/RN 对比）；创建 shared + androidApp + iosApp；双端跑通 Hello KMP | Xcode 与 Gradle 接线（embedAndSignAppleFrameworkForXcode）；JDK/工具链；Kotlin/Native 依赖下载慢的镜像预案；模拟器架构 | 双端模拟器/真机各跑通一次 |
| K1 | 源集与 expect/actual | commonMain/androidMain/iosMain 层级与依赖规则；expect/actual 抽象平台差异；平台信息 API 实战 | 源集依赖层级写错；default hierarchy 误区 | 双端显示各自平台信息 |
| K2 | 共享网络层 | Ktor + kotlinx.serialization；WanAndroid API 统一响应封装；对照 S2 的 Retrofit 写法；commonTest + MockEngine 单测 | 未知字段反序列化崩溃；iOS 端抓包与证书；错误处理双端语义差异 | 单测全绿 + 双端日志打印真实文章数据 |
| K3 | 共享数据与仓库层 | SQLDelight 建表/查询/双端 driver；multiplatform-settings；Repository 模式；内存数据库单测 | 双端 driver 初始化差异；kotlinx-datetime 时区；Paging 3 不上 KMP 的选型取舍 | 单测全绿（含收藏数据本地读写、进程重启后仍在，以内存/文件数据库单测验证） |
| K4 | 共享业务与状态层 | UseCase；共享 ViewModel（androidx.lifecycle KMP）；Flow 状态流范式；runTest 测试 VM | iOS Main dispatcher 缺失崩溃；runTest 双端差异；共享到哪层的架构分歧 | VM 单测全绿 |
| K5 | Swift 互操作深水区 | Kotlin→ObjC/Swift 映射规则（泛型擦除、密封类、suspend、Flow、异常）；接入 SKIE 前后逐项对比 | 全课皆坑：命名冲突、kotlinx 类型不可见、异常 vs NSError、Kotlin/Native 内存模型要点与历史包袱 | 在 Swift 侧以 async/await + AsyncSequence 消费共享层 |
| K6 | iOS SwiftUI 接入 | iosApp 完整实现首页列表 + 登录 + 收藏；共享 VM 与 SwiftUI 绑定；生命周期管理 | 引用循环导致共享对象泄漏；主线程约束；Xcode 里调试 Kotlin 代码 | iOS 端完整业务流程真机跑通 |
| K7 | Android Compose 接入 | androidApp 复用 S 线经验实现同等功能；Koin 替代 Hilt 的差异对照；双工程并存的依赖管理 | 两套工程依赖版本漂移；Koin 无编译期校验的排查套路 | Android 端完整业务流程真机跑通，双端行为一致 |
| K8 | XCFramework 打包与存量 iOS 工程接入 | assembleXCFramework；SPM 本地/远程分发；CocoaPods 方式对比；版本管理与产物体积 | debug/release 产物混用；模拟器 arm64 切片；dSYM 与崩溃符号化 | 新建一个空白 iOS 工程，通过 SPM 接入共享模块并调通 |
| K9 | Compose Multiplatform 拓展视野 | 选一个简单页面用 CMP 跑到 iOS；认识 CMP 边界、成熟度与趋势 | 仅体验，不深入 | CMP 页面在 iOS 模拟器显示 |

**节奏**：每次会话推进一课（K5 等大课可拆两课时，文档按 `K5a/K5b` 拆分）。固定流程：Claude 写代码和文档 → 沙箱可验证部分先跑绿 → 学员本地双端验收 → 按 checklist 确认 → 现场排查问题 → 更新进度文件。学员未确认验收前不推进下一课。

## 6. 交付物与进度机制

### 每课固定三件套

1. **代码**：`WanReaderKMP/` 内可运行增量，一课一批 git 提交（提交信息不加 Co-Authored-By）
2. **教学文档**：`WanReaderKMP/doc/K{N}_主题.md`——概念讲解（iOS 视角对照）→ 动手步骤 → 坑点专节（现象/根因/修复）→ 验收 checklist。每课时必有配套文档，不允许无文档沉淀的课时
3. **验收 checklist**：附于文档末尾，学员本地逐项确认（iOS 端必须模拟器/真机跑通）

### 进度记录

`WanReaderKMP/教学进度.md`（独立于 `学习进度.md` 与 `WanReader/教学进度.md`）：

- 总览表：10 课状态（✅/🔄/⬜）+ 完成日期 + 文档链接
- 日期日志：每次会话完成项、卡点与修复过程
- next step：下次会话入口
- 纪律：会话开始先读、结束必更新

## 7. 测试策略

- 共享模块从 K2 起全程带 commonTest：网络层用 Ktor MockEngine、仓库层用内存数据库、VM 用 runTest；每课新增逻辑必有单测，沙箱先跑绿再交学员
- 双端 UI 不写自动化测试（教学阶段收益低），靠每课验收 checklist 人工验证

## 8. 风险与对策

| 风险 | 对策 |
|---|---|
| Kotlin/Native 工具链与依赖下载慢/失败 | K0 文档给镜像与缓存预案（参照 S0 处理 Gradle 镜像的经验） |
| 沙箱无 Android SDK / Xcode，无法编译验证双端壳 | 共享模块单测在沙箱以 JVM 方式运行（shared 模块保留 jvm() 目标专供测试）；双端验收依赖学员本地确认，未确认不推进 |
| KMP 生态版本迭代快，教学时选型可能过时 | 开课时（S3 完成后）核对各库最新稳定版再锁定 Version Catalog；文档讲"如何自行判断 KMP 库兼容性" |
| 玩Android API 偶发不稳定 | K2 统一错误处理兜底 |

## 9. 已确认的决策记录

| 决策点 | 结论 |
|---|---|
| KMP 技术路线 | 共享逻辑 + 双端原生 UI（SwiftUI / Compose），不走 CMP 全共享 |
| 项目载体 | WanReaderKMP 独立工程，玩Android API |
| 开课时机 | 设计现在定稿；WanReader S 线学到 S3 完成后正式开课 |
| 企业级专题 | 含 XCFramework 打包与存量 iOS 工程接入（K8）、CMP 拓展视野（K9）；CI/CD 与深度性能调试不单独开课，内存模型/崩溃解读等必要内容揉进 K5/K6 |
| 数据库 | SQLDelight（课内对比 Room KMP） |
| DI | Koin |
| 共享层边界 | 共享到 ViewModel（androidx.lifecycle KMP 版），课内讲解企业分歧 |
| Swift 互操作 | 接入 SKIE，K5 做前后对比教学 |
| 功能范围 | 列表 + 登录 + 收藏 + 详情，不做 Paging 3 |
| 课程编排 | 方案 A：自底向上分层推进，共 10 课 |
