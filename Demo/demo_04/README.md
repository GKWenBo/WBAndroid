# demo_04 · Week 3：MVVM + 网络 + Room + Koin + Coil

第 3 周练习工程。在 demo_03（Compose UI）基础上补齐“数据层 + 架构”：
Retrofit 网络请求、Room 本地缓存、ViewModel + StateFlow 的 MVVM、Koin 依赖注入、Coil 图片加载。
数据源：Rick and Morty 公开 API（免费、无需 key、含图片 URL，适合演示列表 + 缓存 + 图片）。

## 架构（单向数据流）
```
UI(Compose) ──> ViewModel(StateFlow) ──> Repository ──> 网络(Retrofit) / 本地(Room)
                  ↑ Koin 负责把 Repository 注入 ViewModel
```
- 列表只观察 Room（数据库单一数据源）：网络数据写入后 Flow 自动重发，旋转屏幕/断网都不丢。
- 对应 iOS：SwiftUI/UIKit + ObservableObject(@Published) + Repository 协议 + Core Data / URLSession。

## 技术栈（2026-07 实测兼容版本）
| 用途 | 库 | 版本 |
|---|---|---|
| 网络 | Retrofit | 2.11.0 |
| HTTP 引擎 | OkHttp + logging-interceptor | 4.12.0 |
| JSON | kotlinx.serialization-json + plugin.serialization | 1.10.0 |
| 本地库 | Room（KSP 编译） | 2.6.1 |
| 注入 | Koin（koin-android / koin-androidx-compose） | 4.2.2 |
| 图片 | Coil（coil-compose 3.x + coil-network-okhttp） | 3.5.0 |
| 注解处理 | KSP（对应 Kotlin 2.3.x） | 2.3.10 |

> Kotlin 2.3.20 / AGP 9.2.1 与 demo_03 一致；模块仍【不声明】kotlin.android（由 AGP 自动应用）。

## 运行 & 检查点
1. Android Studio 打开 `Demo/demo_04` → Sync → Run（需联网，首次加载角色列表）。
2. ✅ 检查点（对应 Week 3 目标）：
   - 列表加载成功，头像用 Coil 显示；点击进入详情。
   - 杀掉 App 重开，列表仍能显示（Room 缓存生效）。
   - 开飞行模式后重开，列表显示上次缓存（断网兜底）。
   - Logcat 过滤 `okhttp` 可见请求日志（logging-interceptor）。

## iOS 工程师易踩坑
- **Room 必须用 KSP**，不是 kapt；KSP 版本要匹配 Kotlin 版本（2.3.10 ↔ 2.3.x）。
- **Koin 取 ViewModel 用 `koinViewModel()`**，不是标准 `viewModel()`（后者找不到构造参数会崩）。
- **`collectAsStateWithLifecycle`** 来自 lifecycle-runtime-compose，比 `collectAsState` 更省电（后台停止收集）。
- **Retrofit 的 suspend 函数**在后台线程跑，不要自己切线程；ViewModel 用 viewModelScope 启动。
- **网络权限**：`AndroidManifest.xml` 已加 `INTERNET`，否则请求静默失败。
- **序列化插件**：用 `@Serializable` 必须应用 `kotlinx.serialization` 插件，否则编译期找不到序列化器。

## 文件地图
- `data/remote/model` —— DTO(`@Serializable`) + Domain + Mapper
- `data/remote` —— RickAndMortyApi(Retrofit 接口) + RetrofitClient(单例)
- `data/local` —— CharacterEntity / CharacterDao / AppDatabase(Room)
- `data/repository` —— CharacterRepository 接口 + Impl(网络+本地合并)
- `di` —— AppModule(Koin 模块)
- `ui/characters` —— CharactersViewModel + 列表/详情 Compose
- `ui` —— AppNavigation(导航) + theme/Theme
- `MyApplication` —— startKoin 初始化 Koin
