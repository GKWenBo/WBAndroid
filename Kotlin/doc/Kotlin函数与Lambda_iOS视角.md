# Kotlin 函数与 Lambda（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 的函数和 Swift 高度相似（一等公民、闭包、尾随 lambda 都有），最大的心智差异有两个——**Kotlin 没有参数标签系统**（命名参数是调用方可选的），以及 **lambda 捕获外部变量后可以直接修改**（没有 Swift 值捕获/`capture list` 那套规则）。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证。

---

## 1. 函数声明基础

```kotlin
// 完整形式：fun 关键字，返回类型写在后面（和 Swift 的 -> 位置不同）
fun greet(name: String, greeting: String = "Hello"): String {
    return "$greeting, $name!"
}

// 单表达式函数：省略大括号和 return，返回类型可推断
fun square(x: Int): Int = x * x

greet("Alice")                          // "Hello, Alice!"（使用默认参数）
greet("Bob", "Hi")                      // "Hi, Bob!"
greet(greeting = "Hey", name = "Carol") // "Hey, Carol!"（命名参数，顺序随意）
```

| 特性 | Kotlin | Swift |
|---|---|---|
| 声明关键字 | `fun` | `func` |
| 返回类型 | `fun f(): Int` | `func f() -> Int` |
| 默认参数 | `greeting: String = "Hello"` | `greeting: String = "Hello"` |
| 参数标签 | 无（调用时**可选**用命名参数） | 有（默认**强制**外部标签） |
| 单表达式简写 | `fun square(x: Int) = x * x` | 无（单表达式可省 `return`） |

**Swift 开发者最大的不适应点**：Kotlin 调用 `greet("Bob", "Hi")` 时看不到参数名，可读性靠调用方自觉写命名参数。团队实践里，**布尔参数、多个同类型参数建议强制写命名参数**，如 `copy(includeHidden = true)`，否则代码评审时根本看不懂。

**默认参数替代了重载**：Swift 里你可能写多个便利方法，Kotlin 直接一个函数配多个默认值。这也是为什么 Kotlin 代码里 `init(...)` 风格的多重载构造器很少见。

---

## 2. vararg 可变参数

```kotlin
fun sumAll(vararg numbers: Int): Int = numbers.sum()

sumAll(1, 2, 3)        // 6，同 Swift 的 numbers: Int...

// 展开已有数组要用 * 操作符（Swift 的可变参数做不到这一点！）
val arr = intArrayOf(4, 5)
sumAll(*arr)           // 9
```

Swift 的可变参数无法直接传入一个已有数组，Kotlin 用 `*`（spread 操作符）解决了这个问题——这在转发参数时很有用。

---

## 3. 函数类型与高阶函数

```kotlin
// 函数类型写法：(参数类型) -> 返回类型，和 Swift 一样
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// 把 lambda 存进变量
val add: (Int, Int) -> Int = { x, y -> x + y }
calculate(3, 4, add)                    // 7

// 尾随 lambda（trailing closure），和 Swift 完全一致
calculate(3, 4) { x, y -> x * y }       // 12

// 函数引用：:: 相当于 Swift 直接传方法名
calculate(3, 4, Int::times)             // 12
```

| Kotlin | Swift | 说明 |
|---|---|---|
| `(Int, String) -> Bool` | `(Int, String) -> Bool` | 函数类型写法一致 |
| `{ x, y -> x + y }` | `{ x, y in x + y }` | `->` vs `in` |
| `{ it * 2 }` | `{ $0 * 2 }` | 单参数隐式名 |
| `String::length` | `\.count`（KeyPath）/ 方法名 | 成员引用 |
| 尾随 lambda | trailing closure | 一致 |

**`it` 隐式参数**：lambda 只有一个参数时可省略参数声明，用 `it` 指代（对应 Swift 的 `$0`）：

```kotlin
listOf(1, 2, 3).map { it * 2 }   // [2, 4, 6]
```

**注意**：嵌套 lambda 时 `it` 会指代最内层的参数，容易混淆——嵌套时应显式命名参数（`{ user -> ... }`），这和 Swift 里嵌套闭包避免用 `$0` 是同一个道理。

---

## 4. 闭包捕获：和 Swift 的关键差异

```kotlin
var counter = 0
val increment = { counter++ }
increment()
increment()
println(counter)  // 2 —— lambda 直接修改了外部的 var！
```

三个和 Swift 不同的点：

1. **可以直接修改捕获的变量**。Swift 闭包捕获 `var` 也能改，但 Kotlin 这里没有值类型/引用类型的心智负担——捕获的变量就是同一个变量。
2. **没有 capture list**。Kotlin 没有 `[weak self]`、`[unowned self]` 这些语法。因为 **JVM 用的是可达性分析 GC，不是引用计数，不存在循环引用导致的内存泄漏**——两个对象互相引用，只要都不可达就会被回收。这是 iOS 开发者转 Kotlin 后可以卸下的最大包袱。
3. **但 Android 有自己的"泄漏"场景**：长生命周期对象（单例、静态变量、还在跑的后台任务）持有 `Activity`/`Fragment` 引用，会让本该销毁的页面无法回收。解法不是 weak 引用满天飞，而是**生命周期感知的作用域**（协程文档里的 `viewModelScope`/`lifecycleScope` 就是干这个的）。

经典闭包工厂示例（验证过）：

```kotlin
fun makeCounter(): () -> Int {
    var count = 0
    return { ++count }   // count 被闭包捕获，生命周期随闭包延长
}

val counter = makeCounter()
counter()  // 1
counter()  // 2
counter()  // 3
```

---

## 5. 可空函数类型（对比 Swift 可选闭包）

```kotlin
var callback: ((String) -> Unit)? = null

// 调用可空函数类型必须用 ?.invoke()，不能直接 callback("x")
callback?.invoke("hello")     // callback 为 null 时什么都不发生

callback = { println(it) }
callback?.invoke("hello")     // 打印 "hello"
```

对应 Swift 的 `var callback: ((String) -> Void)?` 和 `callback?("hello")`。Kotlin 的 `?.invoke()` 写法啰嗦一点，但语义相同。

---

## 6. 局部函数（嵌套函数）

```kotlin
fun processForm(name: String, email: String): String {
    // 函数内部定义函数，捕获外部作用域，Swift 也支持
    fun validate(value: String, fieldName: String): String {
        if (value.isEmpty()) return "$fieldName 不能为空"
        return "OK"
    }

    val nameCheck = validate(name, "name")
    if (nameCheck != "OK") return nameCheck
    return validate(email, "email")
}
```

用途和 Swift 嵌套函数一致：抽取只在当前函数内复用的逻辑，避免污染类的命名空间。

---

## 7. inline 函数与非局部返回（Swift 没有的概念）

Kotlin 的 lambda 在 JVM 上默认会生成一个对象（有分配开销）。标准库大量高阶函数（`forEach`、`map`、`let`、`apply`...）都标记了 `inline`——编译时把 lambda 体直接内联到调用处，**零开销**。

对使用者来说，inline 带来一个重要的语义特性——**非局部返回（non-local return）**：

```kotlin
fun firstEvenOrNull(list: List<Int>): Int? {
    list.forEach {
        if (it % 2 == 0) return it   // 这个 return 直接从 firstEvenOrNull 返回！
    }                                 // 不是从 lambda 返回
    return null
}

firstEvenOrNull(listOf(1, 3, 4, 5))  // 4
```

**这是 Swift 开发者必踩的坑**：Swift 闭包里的 `return` 只结束闭包本身；Kotlin 在 inline 函数的 lambda 里写 `return`，结束的是**外层函数**。如果只想结束当前 lambda（相当于 `continue`），要用标签语法：

```kotlin
list.forEach {
    if (it < 0) return@forEach   // 只跳过当前元素，相当于 continue
    println(it)
}
```

经验法则：lambda 里想写 `return` 时停一秒——想退出整个函数就裸写 `return`，想退出当前这次 lambda 调用就写 `return@函数名`。

---

## 8. 带接收者的函数类型（Kotlin DSL 的基石，Swift 没有直接对应）

```kotlin
// buildString 的参数类型是 StringBuilder.() -> Unit
// 意思是"一个把 StringBuilder 当 this 的 lambda"
val message = buildString {
    append("Hello")    // 这里的 this 是 StringBuilder，直接调它的方法
    append(", ")
    append("Kotlin")
}
// "Hello, Kotlin"
```

`类型.() -> R` 叫**带接收者的函数类型**，lambda 内部的 `this` 就是那个接收者。Gradle KTS、Jetpack Compose、Anko 等所有 Kotlin DSL 都建立在这个特性上。Swift 里最接近的是 `@resultBuilder`（SwiftUI 的 body），但机制完全不同。日常先会用就行，读框架源码时再深究。

---

## 9. 速查小结：从 Swift 迁移的心智提醒

1. **返回类型写法** `fun f(): Int` 而不是 `-> Int`；单表达式函数可以直接 `= 表达式`。
2. **没有强制参数标签**——多参数、布尔参数调用时主动写命名参数，别让代码变成 `update("x", true, false)`。
3. **默认参数 + 命名参数替代重载**，别按 Swift/ObjC 习惯写一堆便利方法。
4. **不需要 `[weak self]`**——JVM 的 GC 没有循环引用问题；但要警惕长生命周期对象持有 Activity。
5. **inline 函数的 lambda 里 `return` 是非局部返回**（退出外层函数），想只退 lambda 用 `return@标签`。这是最容易写出逻辑 bug 的地方。
6. `it` 对应 `$0`，嵌套 lambda 时显式命名参数。
7. 看到 `String.() -> Unit` 这种"类型.开头"的函数类型，就是 DSL 的带接收者 lambda，内部 `this` 是接收者。

---

*相关文档：作用域函数（let/run/apply...）本质就是"inline + 带接收者 lambda"的应用，见《Kotlin扩展与作用域函数_iOS视角.md》；suspend 函数见《Kotlin协程与异步编程_iOS视角.md》。*
