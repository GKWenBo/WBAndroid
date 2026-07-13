# Kotlin 协程与异步编程（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 协程 ≈ Swift Concurrency 的"前辈"（Swift 的 async/await、Task、TaskGroup 设计明显借鉴了它）。对应关系：`suspend` ≈ `async` 函数、`launch` ≈ `Task {}`、`async/await` ≈ `async let`、结构化并发两边理念一致。最大差异：**Kotlin 的取消是协作式且需要理解 `CancellationException`**，以及**没有 actor 隔离/Sendable 检查**——线程安全靠你自己。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证（时间相关示例用 `runTest` 虚拟时间验证）。文末「Android 实战」小节除外。

---

## 0. 概念对照总表

| Kotlin | Swift | 说明 |
|---|---|---|
| `suspend fun` | `func f() async` | 可挂起函数 |
| `launch { }` | `Task { }` | 启动"不要结果"的并发任务 |
| `async { }` + `await()` | `async let` + `await` | 并行取结果 |
| `runBlocking { }` | 无（阻塞桥接，仅 main/测试用） | 同步世界调异步代码 |
| `CoroutineScope` | `TaskGroup` / Task 树 | 结构化并发的容器 |
| `Dispatchers.Main` | `@MainActor` | 主线程 |
| `Dispatchers.IO / Default` | 全局并发执行器（无显式对应） | IO 池 / CPU 池 |
| `withContext(dispatcher)` | 切 actor / executor | 切换执行上下文 |
| `Job.cancel()` | `task.cancel()` | 取消（两边都是协作式） |
| `withTimeout` | 手动实现 | 超时控制 |

---

## 1. suspend 函数：异步代码写成同步样子

```kotlin
suspend fun fetchUser(id: Int): String {
    delay(100)          // 挂起 100ms —— 挂起不是阻塞！线程被让出去干别的
    return "User$id"
}

// 调用处不需要 await 关键字，直接调
val user = fetchUser(1)   // 只是这行必须在协程或另一个 suspend 函数里
```

和 Swift 的区别：**Kotlin 调用挂起函数不写 `await`**。挂起点在 IDE 里以行号栏的箭头图标标识，而不是靠代码里的关键字。习惯 Swift 的"`await` 即挂起点"后，初读 Kotlin 会觉得挂起点"隐形"——注意看 IDE 图标。

`delay()` 对应 `Task.sleep()`：挂起当前协程但**不占用线程**（`Thread.sleep()` 才是阻塞，协程里禁止用）。

---

## 2. 协程构建器：launch、async、runBlocking

```kotlin
// launch：发射后不管，返回 Job（可取消、可 join）—— 对应 Task {}
val job = launch {
    delay(50)
    updateBadge()
}
job.join()      // 需要等它时才 join

// async：要结果的并发，返回 Deferred<T> —— 对应 async let
val deferred = async { fetchUser(1) }
val user = deferred.await()

// runBlocking：阻塞当前线程直到协程完成 —— 只用于 main() 和测试！
fun main() = runBlocking {
    println(fetchUser(1))
}
```

**并行分解**（验证过的耗时对比，用虚拟时间测量）：

```kotlin
// 顺序执行：总耗时 200ms
val a = load("A", 100)   // 先等 100ms
val b = load("B", 100)   // 再等 100ms

// 并行执行：总耗时 100ms —— 对应 Swift 的 async let
val a = async { load("A", 100) }
val b = async { load("B", 100) }
println("${a.await()}+${b.await()}")
```

**Swift 开发者的坑**：`async { }` 一被创建就开始跑（对应 `async let`），不是惰性的；只想封装"以后再跑"的逻辑用 suspend 函数就够了。业务代码里 `launch`/`async` 必须挂在某个 `CoroutineScope` 上——这正是结构化并发。

---

## 3. 结构化并发：协程树与自动取消

理念和 Swift 完全一致：**协程活在作用域里，父取消则子取消，子未完则父不完**。

```kotlin
val parent = launch {
    launch {                       // 子协程
        try {
            delay(1000)
            neverHappens()
        } catch (e: CancellationException) {
            cleanup()              // 收到取消信号
            throw e                // 必须重新抛出！见下文
        }
    }
}
delay(100)
parent.cancelAndJoin()             // 取消父协程 → 子协程连带取消（已验证）
```

`coroutineScope { }` 对应 Swift 的 `withTaskGroup`——套一个作用域，里面所有子协程完成后它才返回，任何一个子协程失败则全部取消：

```kotlin
suspend fun loadScreen(): Screen = coroutineScope {
    val user = async { fetchUser() }
    val feed = async { fetchFeed() }
    Screen(user.await(), feed.await())   // 任一失败 → 另一个自动取消 → 整体抛异常
}
```

---

## 4. 取消：协作式，且有一个 Swift 没有的机制

和 Swift 一样，取消是**协作式**的——协程必须在"检查点"配合退出。所有标准库挂起函数（`delay`、`yield`、IO 操作）都是检查点。纯 CPU 循环要自己检查：

```kotlin
launch {
    while (isActive) {          // 对应 Swift 的 Task.isCancelled 检查
        crunchNumbers()
    }
}
```

**关键差异——取消靠异常传播**：取消发生时，挂起点会抛出 `CancellationException`。由此有两条铁律：

1. **不要吞掉 CancellationException**。`catch (e: Exception)` 会把它一起捕获，导致协程"取消不掉"。捕获后必须重新抛出（如上例），或者精确捕获业务异常类型。
2. 清理逻辑放 `finally`，需要在取消后继续挂起（如上报日志）要用 `withContext(NonCancellable)`。

这是 Kotlin 协程 bug 排行榜第一名，Swift 里没有对应机制（Swift 取消不靠异常，`Task.checkCancellation()` 是显式的）。

---

## 5. 调度器与 withContext：Kotlin 版的"切线程"

```kotlin
suspend fun loadAvatar(): Bitmap =
    withContext(Dispatchers.IO) {        // 切到 IO 线程池
        decodeBitmap(readFile())         // 已验证：这里运行在 worker 线程
    }                                     // 块结束自动切回调用方的调度器
```

| 调度器 | 用途 | iOS 对应心智 |
|---|---|---|
| `Dispatchers.Main` | UI 操作（Android 主线程） | `@MainActor` / main queue |
| `Dispatchers.IO` | 网络、磁盘、数据库 | `DispatchQueue.global()` 做 IO |
| `Dispatchers.Default` | CPU 密集（排序、解析、Diff） | `DispatchQueue.global()` 做计算 |

和 GCD 的最大区别：**`withContext` 是有返回值的、自动切回来的**。iOS 里 `DispatchQueue.main.async` 嵌套回调的场景，Kotlin 是一条直线：

```kotlin
// GCD 心智：dispatch 到后台 → 完成后 dispatch 回主线程
// 协程心智：suspend 函数内部自己保证在正确的线程，调用方无感
suspend fun refresh() {
    val data = withContext(Dispatchers.IO) { api.fetch() }   // 后台
    render(data)                                              // 自动回到调用方上下文
}
```

**最佳实践（官方建议）**："suspend 函数应该主线程安全"——谁做 IO 谁负责在函数**内部** `withContext(IO)`，调用方永远可以放心直接调，不需要关心线程。

**没有 actor/Sendable**：Kotlin 不会像 Swift 6 那样编译期检查数据竞争。多协程共享可变状态要自己负责（用 `Mutex`、`AtomicInteger`、或者最好——不可变数据 + `StateFlow`）。

---

## 6. 超时

```kotlin
val result = withTimeoutOrNull(200) {
    delay(300)
    "ok"
}
// result == null（超时了）；withTimeout(…) 版本则抛 TimeoutCancellationException
```

Swift 没有内置超时，需要 race 两个 Task 手写；Kotlin 一行解决，网络兜底、防 ANR 场景常用。

---

## 7. 异常处理：两种作用域两种行为

**默认（coroutineScope）：一损俱损**（已验证）：

```kotlin
suspend fun riskyParallelLoad(): String = coroutineScope {
    val ok = async { delay(10); "ok" }
    val bad = async<String> { delay(20); throw IllegalStateException("boom") }
    "${ok.await()} ${bad.await()}"
}

try { riskyParallelLoad() } catch (e: IllegalStateException) {
    // 捕获到 "boom"，ok 那个协程也被自动取消了
}
```

**supervisorScope：子协程失败互不影响**（已验证）：

```kotlin
supervisorScope {
    val failing = async<String> { throw IllegalStateException("boom") }
    val healthy = async { delay(10); "ok" }

    try { failing.await() } catch (e: IllegalStateException) { /* 单独处理 */ }
    healthy.await()   // "ok" —— 没有被连带取消
}
```

决策：**业务上"必须全部成功"用 `coroutineScope`；"各干各的、允许部分失败"（如首页多个独立卡片）用 `supervisorScope`**。`viewModelScope` 等 Android 作用域内置的就是 SupervisorJob——一个 launch 崩了不会拖垮整个 ViewModel 的其他协程。

另有 `CoroutineExceptionHandler` 处理 launch 里未捕获的异常（类似全局兜底），配置在作用域上；`async` 的异常则总是在 `await()` 处抛出。

---

## 8. Android 实战（示意代码，需在 Android 工程中运行，未经单测验证）

Android 框架给每个生命周期组件提供了现成的 CoroutineScope，**永远不要自己 `GlobalScope.launch`**（相当于 iOS 里脱离结构化并发裸开 Task.detached）：

```kotlin
// ViewModel 里：viewModelScope，ViewModel 销毁时自动取消所有协程
class UserViewModel(private val repo: UserRepository) : ViewModel() {
    fun loadUser(id: String) {
        viewModelScope.launch {                    // 默认在 Main 调度器
            val user = repo.fetchUser(id)          // repo 内部自己 withContext(IO)
            _uiState.value = UiState.Content(user) // 回到主线程更新状态
        }
    }
}

// Activity/Fragment 里：lifecycleScope
class UserFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            // 配合 repeatOnLifecycle 收集 Flow，见 Flow 文档
        }
    }
}
```

对照 iOS：`viewModelScope` ≈ SwiftUI `.task {}` 修饰符的自动取消行为——页面没了，请求自动取消，不需要手写 `deinit`/`onDisappear` 清理。

依赖：`androidx.lifecycle:lifecycle-viewmodel-ktx`、`lifecycle-runtime-ktx`。

---

## 9. 速查小结：从 Swift 迁移的心智提醒

1. 对照记忆：`suspend`=async 函数、`launch`=Task、`async/await()`=async let、`coroutineScope`=withTaskGroup、`Dispatchers.Main`=@MainActor。
2. **调用挂起函数不写 await**，挂起点看 IDE 行号栏图标。
3. **取消铁律：不要吞 `CancellationException`**——`catch (e: Exception)` 后必须重抛，这是 Kotlin 特有的第一大坑。
4. `withContext` 自动切回，写"主线程安全的 suspend 函数"（IO 切换封装在函数内部），调用方无脑直调。
5. 协程里禁止 `Thread.sleep()`/同步 IO——那是阻塞线程，不是挂起。
6. 没有 Sendable/actor 检查，共享可变状态自己负责；首选不可变数据 + StateFlow。
7. "全部成功"用 `coroutineScope`，"允许部分失败"用 `supervisorScope`。
8. Android 里只用 `viewModelScope`/`lifecycleScope`，不碰 `GlobalScope`。

---

*相关文档：异步数据流（协程版的 Combine/AsyncSequence）见《KotlinFlow响应式编程_iOS视角.md》；lambda/闭包基础见《Kotlin函数与Lambda_iOS视角.md》。*
