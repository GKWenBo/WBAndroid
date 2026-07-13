# Kotlin 空安全与类型系统（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 的空安全和 Swift Optional **理念完全相同、语法几乎一一对应**，可以直接迁移经验。真正的差异在两处：Kotlin 的智能转换（smart cast）不需要 `if let` 引入新变量；以及和 Java 互操作时会出现**平台类型**——编译器放弃检查的"裸奔区"，这是 Swift 开发者没有的风险。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证。

---

## 1. 语法对照总表

| 概念 | Kotlin | Swift |
|---|---|---|
| 可空类型声明 | `String?` | `String?` |
| 安全调用 | `s?.length` | `s?.count` |
| 默认值 | `s ?: "default"`（Elvis） | `s ?? "default"` |
| 强制解包 | `s!!` | `s!` |
| 安全转换 | `x as? String` | `x as? String` |
| 非空才执行 | `s?.let { ... }` | `if let` / `guard let` |
| 链式调用 | `a?.b?.c` | `a?.b?.c` |
| 延迟初始化 | `lateinit var` | `var x: T!`（IUO） |
| 惰性初始化 | `by lazy { }` | `lazy var` |

```kotlin
val name: String? = null

name?.length                 // null（安全调用，整条表达式变为可空）
name?.length ?: 0            // 0（Elvis 给默认值）
name?.uppercase()?.take(3)   // 链式安全调用

val s: String? = null
s!!.length                   // 抛 NullPointerException！等价于 Swift 的 s!
```

**`!!` 的团队约定和 Swift 的 `!` 一样：出现即代码坏味道**。几乎所有 `!!` 都能用 `?:`、`requireNotNull()`、`checkNotNull()`（带错误信息）或重构消掉。

---

## 2. `?.let`：Kotlin 版的 if let

```kotlin
val nullable: String? = "data"

// 只在非空时执行，it 是解包后的非空值
nullable?.let {
    println("收到: $it")
}

// 配合 Elvis 实现 if-let-else
val message = nullable?.let { "处理: $it" } ?: "无数据"
```

对应关系：

```swift
// Swift
if let value = nullable { print("收到: \(value)") }
let message = nullable.map { "处理: \($0)" } ?? "无数据"
```

**什么时候用 `?.let`，什么时候用 `if (x != null)`**：`let` 适合表达式风格（转换出一个值）；普通的分支逻辑用 `if` 更直白。别把 `?.let` 当万能钥匙套娃——嵌套两层以上的 `let` 可读性就崩了，改用提前返回：

```kotlin
val user = findUser(id) ?: return          // 对应 guard let ... else { return }
val email = user.email ?: return
// 这里 user、email 都是非空类型了
```

**Elvis + `return`/`throw` 就是 Kotlin 的 `guard let`**——这是 Swift 开发者最该掌握的惯用法。

---

## 3. 智能转换（smart cast）：比 if let 更进一步

Swift 的 `if let` 需要绑定一个新变量；Kotlin 编译器直接**在检查过的作用域里改变变量的类型**：

```kotlin
val s: String? = "hello"
if (s != null) {
    // 这个块里 s 自动是 String（非空），直接用，不需要新变量
    println(s.length)
}
```

对类型判断同样生效，`when` + `is` 是最常见的形态：

```kotlin
fun describe(x: Any): String = when (x) {
    is String -> "字符串，长度 ${x.length}"   // x 在此分支自动是 String
    is Int -> "整数，加一是 ${x + 1}"         // 这里自动是 Int
    else -> "未知类型"
}
```

**智能转换失效的情况**（编译器会报错提示）：`var` 属性、跨函数、可被其他线程修改的类成员——因为检查和使用之间值可能变了。这时才需要 `?.let` 或局部变量快照：

```kotlin
class Screen {
    var title: String? = null

    fun show() {
        // if (title != null) println(title.length)  // 编译错误：成员 var 不能智能转换
        val t = title ?: return                      // 快照到局部 val
        println(t.length)
    }
}
```

---

## 4. as? 安全转换

```kotlin
val obj: Any = "hello"

obj as? String    // "hello"
obj as? Int       // null（失败返回 null，不崩溃）
obj as Int        // 抛 ClassCastException（对应 Swift 的 as! 崩溃）
```

和 Swift 的 `as?`/`as!` 完全一致。惯用组合：`(x as? Foo)?.doSomething() ?: fallback`。

---

## 5. lateinit vs lazy（对应 IUO 和 lazy var）

```kotlin
class UserSession {
    lateinit var token: String            // 声明"我保证用之前会赋值"

    fun isLoggedIn() = ::token.isInitialized   // 可以检查是否已初始化
}

val session = UserSession()
session.token          // 未初始化就访问：UninitializedPropertyAccessException
session.token = "abc"
session.token          // "abc"
```

```kotlin
// lazy：首次访问时才执行，且只执行一次（默认线程安全）
val config: String by lazy {
    println("初始化了！")   // 只会打印一次
    loadConfigFromDisk()
}
```

| 需求 | Kotlin | Swift | 选择建议 |
|---|---|---|---|
| 生命周期晚于构造（如 View 绑定、DI 注入） | `lateinit var` | `var x: T!` | 必须是 `var`、非空、引用类型 |
| 昂贵对象按需创建 | `val x by lazy {}` | `lazy var` | 只读场景优先用它 |

**Android 场景**：`lateinit` 大量出现在 Fragment 的 view 绑定、依赖注入字段上——和 iOS 里 `@IBOutlet var label: UILabel!` 是同一个存在意义，风险也相同（生命周期外访问就崩）。能用 `lazy` 或构造器注入就别用 `lateinit`。

---

## 6. 特殊类型：Any / Unit / Nothing

| Kotlin | Swift | 说明 |
|---|---|---|
| `Any` | `Any` | 所有非空类型的根；`Any?` 才包含 null |
| `Unit` | `Void`/`()` | "没有有意义的返回值"，可省略不写 |
| `Nothing` | `Never` | 永不正常返回（抛异常/死循环） |

`Nothing` 最实用的模式——Elvis 右侧接一个"必炸"函数，编译器知道后续代码只在非空时执行：

```kotlin
fun fail(message: String): Nothing = throw IllegalStateException(message)

val input: String? = getInput()
val value: String = input ?: fail("input 不能为空")
// 走到这里 value 一定非空，类型是 String
```

标准库的 `error("msg")`、`TODO()` 返回的都是 `Nothing`，对应 Swift 的 `fatalError()`。

---

## 7. 可空集合的两个层次（容易混淆）

```kotlin
val listOfNullable: List<Int?> = listOf(1, null, 3)   // 元素可空
val nullableList: List<Int>? = null                    // 集合本身可空

listOfNullable.filterNotNull()      // [1, 3] —— 对应 Swift compactMap { $0 }
nullableList.isNullOrEmpty()        // true —— 专门处理可空集合的扩展，null 上也能调
nullableList?.size ?: 0             // 0
```

`List<Int?>?` 两层都可空也是合法的（通常说明 API 设计有问题）。`isNullOrEmpty()`/`orEmpty()` 这类**可空接收者扩展**能直接在 null 引用上调用，是 Kotlin 特有的便利（原理见扩展函数文档）。

---

## 8. 解析失败返回 null 的惯用 API

```kotlin
"42".toIntOrNull()     // 42
"abc".toIntOrNull()    // null（不抛异常）

listOf<Int>().firstOrNull()   // null（对比 first() 抛异常）
mapOf("a" to 1)["b"]          // null（Map 取值天然可空）
```

Kotlin 标准库的命名规律：**`xxxOrNull()` 后缀 = 失败返回 null 的安全版本**。与 Swift 的 `Int("abc") == nil` 思路一致。优先用 OrNull 系列 + Elvis，而不是 try/catch。

---

## 9. 平台类型：Swift 开发者没见过的坑

调用 **Java 代码**（没有空安全标注）返回的值，类型显示为 `String!`（注意只有一个感叹号，且**只出现在 IDE 提示里，代码里写不出来**）——这叫**平台类型**：编译器不知道它可不可空，**放弃检查，由你负责**。

```kotlin
// Java: public String getName() { return maybeNull; }
val name = javaObj.name        // 类型是 String!（平台类型）
name.length                    // 编译通过，但运行时可能 NPE！
val safe: String? = javaObj.name   // 主动声明为可空，恢复编译器保护
```

对比：Swift 调 ObjC 未标注 nullability 的 API 时得到 IUO（`String!`），风险性质一模一样。**对策也一样：在边界处主动写明类型**——接收 Java 返回值时显式声明 `String?`，把不确定性挡在入口。详见《KotlinJava互操作_iOS视角.md》。

---

## 10. 速查小结：从 Swift 迁移的心智提醒

1. `?.`、`?:`、`!!`、`as?` 与 Swift 的 `?.`、`??`、`!`、`as?` 一一对应，经验直接平移。
2. **`?: return` / `?: throw` 就是 guard let**——最高频的惯用法，优先于嵌套 `?.let`。
3. 智能转换免去了 `if let` 的变量绑定，但对**成员 var 失效**——快照成局部 `val` 再判空。
4. `!!` 等同于 Swift 强制解包的坏味道，用 `requireNotNull(x) { "原因" }` 至少留下错误信息。
5. `lateinit` ≈ IUO（`T!`），`by lazy` ≈ `lazy var`；能 lazy 不 lateinit。
6. `xxxOrNull()` 后缀是标准库安全 API 的命名规律，优先于 try/catch。
7. **警惕平台类型**：Java 边界返回值主动标 `String?`，别让"裸奔类型"扩散到业务代码里。

---

*相关文档：可空接收者扩展的原理见《Kotlin扩展与作用域函数_iOS视角.md》；平台类型与 Java 互操作细节见《KotlinJava互操作_iOS视角.md》。*
