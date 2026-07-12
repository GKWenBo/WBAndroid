# WanReader · 企业级 Android 从 0 到 1 教学项目 · 设计文档

> 日期：2026-07-12
> 状态：已与学员逐节确认通过
> 学员背景：资深 iOS 工程师（Swift/SwiftUI/MVVM/Combine），已系统学习 Kotlin

## 1. 目标与定位

通过一个完整的企业级项目 **WanReader（玩安卓客户端）**，带学员从 0 到 1 掌握现代 Android 开发，覆盖：

1. 真实企业项目形态的 App 开发（主流架构目录）
2. 项目配置详解及常见坑与解决方案
3. 多渠道打包（国内商店为主）
4. 分发与上架流程
5. 全程记录教学进度

**与现有 4 周学习路线（`学习路线.md` / `Demo/demo_0x`）完全独立**，互不影响、各自记进度。

## 2. 项目定位

- **项目名**：WanReader，包名 `com.wb.wanreader`
- **位置**：仓库根目录 `WanReader/`，与 `Demo/`、`Kotlin/` 平级
- **数据源**：玩Android（WanAndroid）免费开放 API——真实接口、真实 Cookie 登录态、分页，无需自建后端

### V1.0 功能范围（最终"可上架"形态）

- 底部三 Tab：首页（Banner + 置顶 + 文章流分页）、项目/广场、我的
- 登录 / 注册 / 退出（真实 Cookie 会话）
- 搜索（热词 + 结果分页）
- 文章详情（WebView）、收藏 / 取消收藏、我的收藏列表
- 设置（深色模式、清缓存、关于页）
- 离线缓存（断网可看已缓存内容）

## 3. 技术栈（对齐 2026 国内企业新项目主流）

| 领域 | 选型 | iOS 对照 |
|---|---|---|
| 语言/并发 | Kotlin + Coroutines/Flow | Swift + async/await/Combine |
| UI | Jetpack Compose + Material 3（另设 XML 专题） | SwiftUI |
| 架构 | 官方架构指南 MVVM（UI / Domain / Data 三层） | MVVM + Repository |
| DI | Hilt | 无直接对应，教学中以 iOS 视角拆解 |
| 网络 | Retrofit + OkHttp + kotlinx.serialization | URLSession + Codable |
| 分页 | Paging 3 | 手写分页 |
| 本地 | Room + DataStore | Core Data + UserDefaults |
| 图片 | Coil | Kingfisher/SDWebImage |
| 构建 | Gradle Kotlin DSL + Version Catalog，后期 build-logic | SPM + xcconfig |

### 模块策略

**先单模块、后演进多模块**：前期单 `app` 模块 + 标准分层包结构完成业务；S8 专门演示"为什么拆、怎么拆"多模块（`core-*` + `feature-*` + build-logic convention plugins），复现真实项目演化过程。

### 单模块期目录结构

```
com.wb.wanreader
├── WanApp.kt / MainActivity.kt
├── ui/         # UI 层：navigation、theme、components + 各功能 screen/ViewModel
│   ├── home/  square/  mine/  login/  search/  detail/  settings/
├── domain/     # 领域模型 + UseCase（薄层）
├── data/       # Repository + network(api/dto/拦截器) + local(room/datastore)
└── util/
```

## 4. 教学模式

- **我写你学**：每阶段由 Claude 产出代码 + iOS 视角讲解文档；学员本地 Android Studio Sync、真机跑通、按验收标准确认、随时提问
- **坑在现场讲**：配置坑、运行时坑均安排在会真实遇到它的阶段现场讲解与排查，排查过程本身作为教学素材记入文档

## 5. 阶段计划（方案 A：功能驱动线性闯关，共 11 阶段）

每阶段交付可真机验收的增量 + 一份教学文档。

| # | 阶段 | 核心内容 | 现场讲的坑（举例） | 真机验收标准 |
|---|---|---|---|---|
| S0 | 工程初始化与配置 | 新建工程；Gradle 全家桶逐文件讲透（settings / 根 build / app build / wrapper / libs.versions.toml）；AGP↔Kotlin↔compileSdk 版本关系；.gitignore | Kotlin 插件重复注入；Gradle 镜像；JDK 版本不匹配 | 模板 App 真机跑通 |
| S1 | 架构骨架 | 分层包结构落地；Hilt 接入；Material 3 主题 + 深色模式；Navigation + 底部三 Tab 空页 | kapt/ksp 选择；Hilt 注解报错排查套路 | 三 Tab 切换 + 跟随系统深色模式 |
| S2 | 网络层 | Retrofit + OkHttp + kotlinx.serialization；BaseResponse 统一封装与错误码处理；日志/公共参数拦截器 | 明文流量；序列化字段缺省崩溃；主线程网络异常 | Logcat 打印首页文章真实数据 |
| S3 | 首页列表 | ViewModel + StateFlow 范式定型；Paging 3；Banner+置顶+文章流合并；下拉刷新、加载/空/错误三态；单元测试教学①（ViewModel：coroutines-test + Turbine） | Paging 与刷新配合；重组性能 | 首页无限滚动流畅、断网显示重试 |
| S4 | 登录与会话 | 登录/注册页与表单校验；CookieJar 持久化；DataStore 存用户信息；全局登录态 | 杀进程后登录态丢失；Cookie 域匹配 | 登录 → 杀进程重开仍是登录态 |
| S5 | 详情与收藏 | WebView 详情页；收藏/取消收藏 + 乐观更新与失败回滚；未登录操作拦截跳登录 | WebView 生命周期泄漏、返回键劫持 | 收藏在列表/收藏页/详情间状态一致 |
| S6 | 本地缓存 | Room 建库；离线优先策略；清缓存；单元测试教学②（Repository：MockWebServer） | Room 迁移崩溃；Flow 双数据源合并 | 飞行模式下首页仍可浏览 |
| S7 | XML/View 专题 | Activity/Fragment + RecyclerView + ViewBinding 重做积分排行页；生命周期对照 UIViewController；Compose↔XML 互操作 | Fragment 重建陷阱 | 混合技术栈页面正常运行 |
| S8 | 多模块演进 | 拆 core-*（common/network/database/designsystem）+ feature-*；build-logic convention plugin；api vs implementation | 循环依赖；资源命名冲突 | 拆分后功能与拆分前完全一致 |
| S9 | 构建变体与多渠道 | buildTypes、签名 keystore、R8 混淆与 mapping 回溯、productFlavors 与注入式渠道（Walle/VasDolly）对比选型、versionCode 策略、APK 产物分析 | 混淆后序列化崩溃；签名丢失后果 | 一条命令产出多渠道 release 包并装机 |
| S10 | 分发与上架 | 加固；隐私合规（隐私弹窗时机、SDK 合规、targetSdk 要求）；软著/ICP 备案；华为/小米/OPPO/vivo/应用宝上架流程；Google Play + AAB 对照简介 | 隐私政策被驳回常见点 | 产出可直接执行的上架 checklist |

**节奏**：每次会话推进一个阶段（S3、S9 等大阶段可拆两次）。固定流程：Claude 写代码和文档 → 学员本地 Sync 真机跑通 → 按验收标准确认 → 有问题现场排查 → 更新进度文件。学员未确认验收前不推进下一阶段。

**S10 说明**：实际上架需开发者账号与真实资质，教学以"流程 + 材料准备 + checklist"为交付，可直接复用于真实项目。

## 6. 交付物与进度机制

### 每阶段固定三件套

1. **代码**：`WanReader/` 内可运行增量，注释解释"为什么"
2. **教学文档**：`WanReader/doc/S{N}_主题.md`，iOS 视角对照，含"坑与解决方案"小节
3. **验收 checklist**：附于文档末尾，学员真机逐项确认

### 进度记录

`WanReader/教学进度.md`（独立于现有 `学习进度.md`）：

- 总览表：11 阶段状态（✅/🔄/⬜）+ 完成日期
- 日期日志：每次会话完成项、卡点与修复过程
- next step：下次会话入口
- 纪律：会话开始先读、结束必更新

## 7. 测试策略

不做全量覆盖。安排两次单元测试教学（见阶段表 S3、S6），对照 XCTest 讲 JUnit 生态，让学员见过企业项目测试形态，够用即止。

## 8. 风险与对策

| 风险 | 对策 |
|---|---|
| 玩Android API 偶发不稳定 | S2 统一错误处理兜底；S6 后离线缓存免疫 |
| Claude 沙箱无 Android SDK，无法本地编译验证 | 每阶段验收依赖学员本地 Android Studio + 真机确认（demo_02/03 已验证的协作方式），未确认不推进 |
| 依赖版本时效 | Version Catalog 集中管理；S0 讲解"如何自行判断版本兼容性" |

## 9. 已确认的决策记录

| 决策点 | 结论 |
|---|---|
| 与现有 4 周路线关系 | 完全独立新工程 |
| 项目类型 | 技术社区客户端（玩Android API） |
| 模块结构 | 先单模块，S8 演进多模块 |
| DI | Hilt |
| 分发市场 | 国内商店为主，Google Play 对照简介 |
| UI 体系 | Compose 为主 + S7 XML 专题 |
| 教学模式 | 我写你学 |
| 阶段编排 | 方案 A：功能驱动线性闯关 |
