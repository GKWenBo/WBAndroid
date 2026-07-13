# Kotlin Flow 响应式编程（面向 Swift/iOS 开发者）

> 核心思路：Flow 一个框架同时覆盖了 iOS 的两套东西——**冷流 `Flow` ≈ `AsyncSequence`（一次性异步序列），热流 `StateFlow`/`SharedFlow` ≈ Combine 的 `CurrentValueSubject`/`PassthroughSubject`**。掌握"冷热"之分，就掌握了 Flow 的一半；另一半是操作符，和 Combine 高度对应。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证（时间相关示例用 `runTest` 虚拟时间验证）。文末「Android 实战」小节除外。

---

## 0. 概念对照总表

| Kotlin | iOS 对应 | 冷/热 |
|---|---|---|
| `Flow<T>` | `AsyncSequence` / Combine `Publisher` | 冷 |
| `flow { emit(...) }` | `AsyncStream` 构建 | 冷 |
| `collect { }` | `for await x in seq` / `sink` | 触发执行 |
| `StateFlow<T>` | `@Published` / `CurrentValueSubject` | 热，有当前值 |
| `SharedFlow<T>` | `PassthroughSubject` | 热，无当前值 |
| `map/filter/debounce/combine/zip` | Combine 同名操作符 | — |
| `flatMapLatest` | `switchToLatest` / `.map().switchToLatest()` | — |
| `catch` | `catch` / `replaceError` | — |
| `flowOn` | `subscribe(on:)`（近似） | — |
| `MutableStateFlow.value = x` | `subject.send(x)` | — |

---

## 1. 冷流：不收集就不执行

```kotlin
val numbers = flow {        // 构建块是 suspend 的，可以随意 delay/请求网络
    emit(1)
    delay(10)
    emit(2)
    delay(10)
    emit(3)
}

numbers.toList()            // [1, 2, 3] —— collect/toList 才触发执行
```

**冷的含义**（已验证）：构建块在每次收集时**从头重新执行**，没有收集者就完全不执行：

```kotlin
var startCount = 0
val f = flow {
    startCount++
    emit(1)
}
// 此时 startCount == 0 —— 声明流不执行任何代码
f.collect { }
f.collect { }
// startCount == 2 —— 每个收集者独立执行一遍
```

对照 iOS：和 `AsyncSequence`/Combine 冷 Publisher 行为一致。**一个返回 `Flow` 的函数只是"描述了一个数据管道"**，真正干活在 `collect`。数据库监听（Room 的 `Flow<List<User>>`）、网络轮询都是这么建模的。

---

## 2. 常用操作符（与 Combine 几乎同名）

```kotlin
(1..10).asFlow()
    .filter { it % 2 == 0 }    // 2, 4, 6, 8, 10
    .map { it * it }           // 4, 16, 36, 64, 100
    .take(3)                   // 4, 16, 36 —— 拿够就取消上游
    .toList()
```

**debounce——搜索框防抖**（已验证，用虚拟时间）：

```kotlin
searchQueries
    .debounce(200)     // 停止输入 200ms 后才发出最新值
    .collect { query -> search(query) }

// 输入序列："k"(停50ms) "ko"(停300ms) "kot"(停50ms) "kotlin"(流结束)
// 实际发出：["ko", "kotlin"]
```

**zip vs combine**（已验证）：

```kotlin
val letters = flowOf("A", "B", "C")
val numbers = flowOf(1, 2, 3)

// zip：按序配对，一夫一妻 —— 对应 Combine zip
letters.zip(numbers) { l, n -> "$l$n" }.toList()   // [A1, B2, C3]

// combine：任一方更新就用双方最新值发出 —— 对应 Combine combineLatest
// flow1: A(t=0) B(t=100)；flow2: 1(t=50) 2(t=150)
flow1.combine(flow2) { l, n -> "$l$n" }.toList()   // [A1, B1, B2]
```

**flatMapLatest——"新请求到来，取消旧请求"**（已验证）：

```kotlin
queryFlow.flatMapLatest { query ->
    flow { emit(api.search(query)) }   // 新 query 到来时，旧的搜索流被取消
}
// 上游发 A、(30ms后) B；内部流先发 "X-1"、100ms 后发 "X-2"
// 结果：[A-1, B-1, B-2] —— A-2 永远不会出现，A 的内部流被取消了
```

对应 Combine 的 `switchToLatest`，是"搜索建议""tab 切换加载"的标准姿势。同族还有 `flatMapConcat`（顺序执行）和 `flatMapMerge`（并发合并）。

---

## 3. 错误处理与生命周期回调

```kotlin
flow {
    emit(1)
    throw IllegalStateException("boom")
}
    .catch { e -> emit(-1) }    // 捕获【上游】异常，可发降级值 —— 已验证得 [1, -1]
    .collect { }
```

**`catch` 只管上游**：它捕获不到 `collect { }` 块里的异常——这个设计保证异常责任清晰（对比 Combine 的 `catch` 也是只管上游，心智一致）。

生命周期回调（已验证，顺序为 start → 值1 → 值2 → done）：

```kotlin
flowOf(1, 2)
    .onStart { events.add("start") }        // 对应 handleEvents(receiveSubscription:)
    .onEach { events.add("值$it") }          // 对应 handleEvents(receiveOutput:)
    .onCompletion { events.add("done") }    // 正常/异常结束都会走，参数里有 cause
    .collect()
```

---

## 4. flowOn：只切上游的线程

```kotlin
flow {
    val data = readHugeFile()     // 上游：在 IO 线程执行
    emit(data)
}
    .flowOn(Dispatchers.IO)       // 只影响它【上面】的操作
    .collect { render(it) }       // 下游：保持收集者所在线程（如 Main）
```

已验证：上游代码运行在 `DefaultDispatcher-worker` 线程，`collect` 块保持调用方线程。**`flowOn` 影响上游、`collect` 跟随调用方作用域**——这比 Combine 的 `subscribe(on:)`/`receive(on:)` 双操作符模型更简单：收集方所在协程天然决定了下游线程。

---

## 5. StateFlow：UI 状态的标准载体

对应 `@Published`/`CurrentValueSubject`——**永远持有一个当前值**的热流：

```kotlin
val state = MutableStateFlow(0)   // 必须有初始值

state.value            // 0 —— 随时同步读，Combine 的 CurrentValueSubject.value
state.value = 1        // 直接赋值
state.update { it + 1 }  // 原子更新（并发安全），value 现在是 2
```

三个内建行为（都已验证）：

1. **新收集者立刻收到当前值**（replay=1 的语义）。
2. **相同值被跳过**：连续两次 `state.value = 1`，收集者只收到一次——自带 `distinctUntilChanged`。
3. **永不完结**：`collect` 一个 StateFlow 的协程不会自己结束，作用域负责取消它（这就是 Android 里要配 `repeatOnLifecycle` 的原因）。

```kotlin
val collected = mutableListOf<Int>()
val job = launch { state.collect { collected.add(it) } }
// 依次设置 1, 1, 2 后：collected == [0, 1, 2]（初始值 0 + 去重后的 1、2）
job.cancel()
```

**典型架构**：ViewModel 暴露 `StateFlow<UiState>`，内部持有 `MutableStateFlow`——和 iOS 里 `@Published private(set) var state` 的封装习惯一模一样：

```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()   // 对外只读
```

---

## 6. SharedFlow：一次性事件的载体

对应 `PassthroughSubject`——**没有当前值**的热广播：

```kotlin
val events = MutableSharedFlow<String>()   // 默认 replay = 0

events.emit("订阅前的事件")    // 没有订阅者 → 直接丢弃（已验证）

launch { events.collect { show(it) } }
events.emit("事件1")           // 订阅者收到
```

**StateFlow vs SharedFlow 的选择**（= iOS 里 CurrentValueSubject vs PassthroughSubject 的选择）：

| 场景 | 用 | 原因 |
|---|---|---|
| UI 状态（列表数据、加载中/错误） | `StateFlow` | 新订阅者需要"最新状态"，重复值应去重 |
| 一次性事件（弹 Toast、导航、震动） | `SharedFlow` | 状态重放会导致旋转屏幕后重复弹窗 |

`MutableSharedFlow(replay = n, extraBufferCapacity = m)` 可配置重放和缓冲，`replay=1` 的 SharedFlow 近似 StateFlow 但不去重、可无初始值。

---

## 7. Android 实战（示意代码，需在 Android 工程中运行，未经单测验证）

**ViewModel → UI 的标准管道**：

```kotlin
class UserViewModel(repo: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _toast = MutableSharedFlow<String>()
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.observeUser()                        // Room 返回的 Flow（冷流）
                .map { UiState.Content(it) as UiState }
                .catch { emit(UiState.Error(it.message)) }
                .collect { _uiState.value = it }      // 冷流灌进热流
        }
    }
}
```

**Fragment/Activity 侧收集——必须配 repeatOnLifecycle**：

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        // 进入 STARTED 开始收集，退到后台自动停止，回来自动重启
        launch { viewModel.uiState.collect { render(it) } }
        launch { viewModel.toast.collect { showToast(it) } }
    }
}
```

为什么必须这么写：StateFlow 永不完结，直接 `lifecycleScope.launch { collect }` 会在 App 退到后台后**继续收集、继续刷 UI**，浪费资源甚至崩溃。`repeatOnLifecycle` 相当于 iOS 里"`viewWillAppear` 订阅 + `viewDidDisappear` 取消"的自动化版本。Compose 里对应 `collectAsStateWithLifecycle()` 一行解决。

依赖：`androidx.lifecycle:lifecycle-runtime-ktx:2.6+`。

---

## 8. 速查小结：从 Swift/Combine 迁移的心智提醒

1. **冷热之分是第一课**：`Flow` 是冷的（每个收集者重新执行、不收集不执行），`StateFlow`/`SharedFlow` 是热的（始终活着、共享一份）。
2. 对照记忆：`Flow`=AsyncSequence、`StateFlow`=@Published/CurrentValueSubject、`SharedFlow`=PassthroughSubject、`flatMapLatest`=switchToLatest、`combine`=combineLatest。
3. **UI 状态用 StateFlow，一次性事件用 SharedFlow**——用错的典型症状：转屏后 Toast 重复弹。
4. `catch` 只捕获上游异常，`collect` 块的异常它不管。
5. `flowOn` 只切上游线程；下游线程由收集者所在协程决定，没有 `receive(on:)`。
6. StateFlow 自带去重 + 永不完结——Android 里收集必须配 `repeatOnLifecycle`，否则后台空转。
7. 暴露给外部时用 `asStateFlow()`/`asSharedFlow()` 收掉可变性，对应 `private(set)` 习惯。

---

*相关文档：协程基础（作用域、调度器、取消）见《Kotlin协程与异步编程_iOS视角.md》——Flow 的一切都跑在协程上；惰性求值的同步版见《Kotlin集合开发指南_iOS视角.md》的 Sequence 一节。*
