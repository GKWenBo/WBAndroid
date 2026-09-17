# WanReader S2 网络层实施计划

> 执行方式：沿用已确认的本会话直接实施，使用 executing-plans；代码和配套教学文档一起交付。

**目标**：首页请求真实文章接口，在页面显示结果摘要，并在 Debug Logcat 输出前三条文章。
**架构**：HomeViewModel → ArticleRepository → WanApi → Retrofit/OkHttp。DTO 留在数据层，Repository 返回普通 ArticlePage；NetResult 区分业务、HTTP、网络、解析和协议错误。
**技术栈**：Retrofit 2.11.0、OkHttp 4.12.0、serialization-json 1.8.1、Kotlin serialization 插件 2.3.20、Coroutines 1.10.2、Hilt。
**设计依据**：`docs/superpowers/specs/2026-07-12-wanreader-teaching-design.md` 的 S2。

## 约束

执行结果（2026-09-16）：下列三项任务已完成，10 项测试通过，Debug 构建通过，Lint 0 错误/16 条版本提示警告。首次测试记录为缺少待实现 API 的编译失败，非断言失败。真机验收仍待学员确认。

- 独立工程 `WanReader/`，包名 `com.wb.wanreader`；单模块；Compose + Hilt。
- 保持 compileSdk 36.1 / minSdk 24 / targetSdk 36。
- 每课配教学文档、验收清单和进度记录；真机验收前不进入 S3。
- 使用 `https://wanandroid.com/`；首页参数 `page` 从 0 起；不增加服务端未定义的查询参数。
- 不吞协程取消；只在 Debug 输出 BASIC HTTP 日志及少量公开文章摘要。

## 任务 1：网络契约和边界测试

- [ ] 在版本目录和 app 构建文件接入网络、序列化及 JUnit/MockWebServer 依赖，启用 BuildConfig；Manifest 添加 INTERNET。
- [ ] 新建 `app/src/test/java/com/wb/wanreader/data/ArticleRepositoryTest.kt`，通过真实 Retrofit 连接本地 MockWebServer 验证：GET 路径、Accept 头、未知 JSON 字段、nullable 作者、业务非零、HTTP 503、缺必填字段、空 data、IOException、取消继续抛出。
- [ ] 执行 `./gradlew :app:testDebugUnitTest --tests '*ArticleRepositoryTest'`，记录新增 API 尚未实现导致的失败。
- [ ] 新建 `data/network/{BaseResponse,NetResult,NetworkClient,WanApi}.kt`、`data/network/dto/ArticleDto.kt`、`data/model/ArticlePage.kt` 和 `data/ArticleRepository.kt`。
  对外接口：`suspend fun ArticleRepository.firstPage(): NetResult<ArticlePage>`；成功条件为 errorCode == 0 且 data 非空。
  NetworkClient 提供 `create(baseUrl: String, debug: Boolean, logger: HttpLoggingInterceptor.Logger): WanApi`，生产和测试复用配置。
- [ ] 同一测试命令转绿，避免依赖真实公网做单元测试。

## 任务 2：Hilt 与首页连接

- [ ] `di/NetworkModule.kt` 提供单例 WanApi，Repository 构造注入。
- [ ] HomeViewModel 保留 greeting；新增只读 StateFlow、loadArticles()、首次加载及进行中去重。viewModelScope 管理取消；成功日志限制前三条。
- [ ] HomeScreen 生命周期感知收集状态，展示加载/成功/错误摘要与重试按钮；不实现完整列表或分页。
- [ ] 执行 `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`，检查生成物及失败信息。

## 任务 3：教学交付

- [ ] 新建 `WanReader/doc/S2_网络层与错误处理.md`，覆盖 iOS 对照、文件职责、请求时序、版本配置、DTO/业务模型边界、错误分类、取消和主线程、HTTPS、日志、测试命令、真机验收。
- [ ] 根据 S1 已勾选清单更新 S1 为完成，S2 更新为待真机验收，写入本课实际验证结果。
- [ ] `git diff --check` 后交付；不自动推进 S3，不自动提交或推送。
