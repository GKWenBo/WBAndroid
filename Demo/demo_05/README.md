# demo_05 · Week 4：综合 Mini App（GitHub 用户搜索）

第 4 周综合工程，串联前 3 周全部能力：Compose UI + MVVM + Retrofit + Room + Koin + Coil，
并补充实战话题：**配置变更/生命周期、运行时权限、调试、打包签名**。
数据源：GitHub 公开 Search API（搜索用户，含头像，免费但限 60 次/小时）。

## 架构（与 demo_04 同构，只是换数据源 + 加了搜索/权限/调试）
```
SearchScreen(TextField) ─> SearchViewModel ─> UserRepository
                                                  ├─ GitHubApi(Retrofit)  ── 网络
                                                  └─ CachedUserDao(Room) ── 本地缓存
UserDetailScreen ◀─ Navigation(id) ◀─ 点击列表项
```
- 搜索结果写入 Room，列表观察 Room：断网可浏览、旋转屏幕不丢（ViewModel + 数据库双重保活）。
- 对应 iOS：SwiftUI + @State/@Binding + ObservableObject + Repository + Core Data + 权限 requestAuthorization。

## 新增实战主题
1. **配置变更 / 生命周期**：ViewModel 由 Koin 按生命周期创建，旋转屏幕时状态保留（搜索词、结果不丢）。
   对比 iOS：UIKit 的 `viewWillTransition` / SwiftUI 的 `.navigationDestination` 自适应。
2. **运行时权限**：搜索页有「请求通知权限」按钮，演示 `rememberLauncherForActivityResult` +
   `ActivityResultContracts.RequestPermission`（对应 iOS 的 `requestAuthorization`）。
   注意 minSdk 24 上该权限不适用，系统会立即回调“拒绝”，正好演示拒绝分支。
3. **调试**：
   - Logcat 过滤 `okhttp` 看请求；过滤 `Koin` 看依赖注入日志。
   - 断点：在 `UserRepositoryImpl.search` 下断点查网络/缓存合并逻辑。
   - Profiler（AS 底部 Profiler 标签）：看内存/CPU，验证列表滚动不抖动。
4. **打包签名基础**：`Build → Generate Signed App Bundle / APK`，首次需创建 keystore（jks）。
   生产 keystore 务必备份，丢了无法更新上架应用（对应 iOS 的 p12/Provisioning 管理）。

## 技术栈（同 demo_04，仅数据源不同）
Retrofit 2.11.0 · OkHttp 4.12.0 · kotlinx.serialization 1.10.0 · Room 2.6.1(KSP) ·
Koin 4.2.2 · Coil 3.5.0 · Kotlin 2.3.20 · AGP 9.2.1 · KSP 2.3.10

## 运行 & 检查点
1. Android Studio 打开 `Demo/demo_05` → Sync → Run。
2. ✅ 检查点（对应 Week 4 目标）：
   - 搜索 `torvalds`，列表出现 GitHub 用户，头像用 Coil 加载。
   - 旋转屏幕，搜索结果与输入不丢。
   - 杀掉重开，列表显示上次缓存（Room）。
   - 点「请求通知权限」看 Toast 回调（授予/拒绝）。
   - 搜索一个不存在的词（如 `zzz_nope_xyz`），应进入空列表（无崩溃）。
3. ⚠️ GitHub 未认证限 60 次/小时，频繁搜索会 403；等一会儿或换词即可。

## iOS 工程师易踩坑（同 demo_04 外加几点）
- **参数类型匹配**：导航参数 `NavType.LongType` 对应 `Long`，用 `IntType` 会类型转换崩溃。
- **运行时权限**：Android 13+ 才需要 POST_NOTIFICATIONS；低版本请求会立即拒绝，属正常。
- **API 限流**：网络失败先用 `Result` 兜底，别让 UI 崩溃（对应 iOS 的 do/try-catch）。
- 其余见 demo_04 README。

## 文件地图
- `data/remote/model` —— UserModels(DTO + Domain + Mapper)
- `data/remote` —— GitHubApi + RetrofitClient
- `data/local` —— CachedUserEntity / CachedUserDao / AppDatabase
- `data/repository` —— UserRepository + Impl
- `di` —— AppModule
- `ui/search` —— SearchViewModel + SearchScreen(搜索+权限) + UserDetailScreen
- `ui` —— AppNavigation + theme/Theme
- `MyApplication` —— startKoin

> 至此 4 周路线全部落地：demo_02(Kotlin) → demo_03(Compose) → demo_04(数据架构) → demo_05(综合)。
> 后续可在真实需求中复用这套模板。
