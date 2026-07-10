# Kotlin 速成（对照 Swift 工程师）

> 你已会 Swift。Kotlin 与 Swift 极其相似（都类型推断、都空安全、都有高阶函数）。
> 本文只列**和 Swift 不同 / 容易绊倒**的点，相同点一笔带过。

## 1. 变量与函数
```kotlin
val name = "wenbo"   // val ≈ let（不可重赋，首选）
var age = 18         // var ≈ var（可重赋）
age = 19

// 默认参数 + 命名参数 ≈ Swift 默认参数
fun greet(title: String = "同学", target: String = name) = "Hi $title, $target"
greet(target = "iOSer")  // 命名调用
```
- 行尾**不写分号**。
- 字符串模板 `$var` / `${expr}` ≈ Swift 的 `\(var)`。

## 2. 类与 data class
```kotlin
data class User(val id: Int, val name: String, val isVip: Boolean = false)
val u1 = User(1, "A")
val u2 = u1.copy(name = "B")   // copy 改字段生成新实例 ≈ struct 改字段
```
- `data class` ≈ Swift `struct`：自动生成 `equals`/`hashCode`/`toString`/`copy`。
- 普通 `class` ≈ Swift `class`（引用语义）。Kotlin 里首选 `data class`/`val`，少写可变 `var class`。

## 3. sealed（状态机神器）
```kotlin
sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: List<String>) : UiState
    data class Error(val msg: String) : UiState
}
val desc = when (state) {        // 必须穷尽所有分支，否则编译报错
    is UiState.Loading -> "加载中"
    is UiState.Success -> "成功 ${state.data.size} 条"
    is UiState.Error -> "失败: ${state.msg}"
}
```
- `sealed` ≈ Swift `enum` + 关联值，但每个分支可带不同数据，且是**类层次**（可比 enum 承载更复杂状态）。
- `when` 覆盖 `sealed` 全部分支时**无需 else**，编译器保证穷尽（比 Swift `switch` 还严格）。

## 4. 扩展函数（≈ Swift extension）
```kotlin
fun String.addExclaim() = "$this!"
"Hi".addExclaim()   // "Hi!"
```
- 给已有类型（含 SDK 类型）加方法，无需继承、无需修改源码。

## 5. 空安全（≈ Swift ? / !，但更强制）
```kotlin
var maybe: String? = null
maybe?.length          // 安全调用 ≈ Swift 的 ?.  → 为 null 时整体为 null
maybe?.length ?: 0     // Elvis 提供默认值 ≈ Swift 的 ??
maybe!!.length         // 强制解包 ≈ Swift 的 !，为 null 直接抛异常，慎用
```
- Kotlin 在**编译期**区分可空/非空，比 Swift 的运行时崩溃更早暴露问题。

## 6. 作用域函数 let/run/apply/also/with
| 函数 | 接收者 `this`/`it` | 返回 |
|---|---|---|
| `let` | `it` | lambda 最后一行 |
| `run` | `this` | lambda 最后一行 |
| `apply` | `this` | **对象本身**（用于初始化） |
| `also` | `it` | **对象本身**（用于附带副作用） |
| `with(obj){...}` | `this` | lambda 最后一行 |

```kotlin
val user = User(2, "C").apply { /* 初始化 this */ }
val len = user.name.let { it.length }      // it ≈ Swift 尾随闭包 $0
```

## 7. 高阶函数 + lambda（≈ Swift 闭包）
```kotlin
val nums = listOf(1, 2, 3, 4)
nums.map { it * 2 }                        // ≈ nums.map { $0 * 2 }
nums.filter { it % 2 == 0 }.sum()
// 最后一个 lambda 可挪出括号（尾随 lambda）
nums.forEach { println(it) }
```
- `it` 是单参 lambda 的默认形参名。

## 8. 协程（≈ Swift async/await）
```kotlin
suspend fun fetchUser(): String {          // suspend ≈ async
    delay(500)                             // ≈ Task.sleep，不阻塞线程
    return "User(1)"
}
// 并发 ≈ Swift 的 async let / TaskGroup
suspend fun demo() = coroutineScope {
    val a = async { fetchUser() }
    val b = async { fetchPosts() }
    "${a.await()} + ${b.await()}"
}
```
- 协程是**挂起**不是线程阻塞；`suspend` 函数只能在协程或另一个 `suspend` 里调用。
- `Dispatchers.Main`（主线程）需要 `kotlinx-coroutines-android`；`Dispatchers.Default/IO` 在 `kotlinx-coroutines-core` 即可。
- 在 Android 中优先用 `viewModelScope` / `lifecycleScope`，自动随生命周期取消（≈ 不用手动管 Task 生命周期）。

## 9. object（单例）
```kotlin
object Config { val apiBase = "https://api.x.com" }
Config.apiBase
```
- `object` 是全局唯一单例；还有 `companion object`（≈ Swift 的 `static`/类型方法）。

## 10. 与 Swift 的细微差异速记
- Kotlin **没有** `struct`/`class` 的值/引用语义强制区分，但用 `data class` + `val` 可达成值语义效果。
- 继承用 `:` + `open`/`abstract`；默认类 `final`（≈ Swift 默认 final，需显式 `open`）。
- 没有 `try?`/`try!`，异常用 `try/catch`（Kotlin 异常大多非检查）。
- `List`/`Map` 默认只读（`List<T>`），可变用 `MutableList<T>`（≈ Swift `Array` vs 不变性靠 `let`）。
