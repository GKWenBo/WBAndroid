# Kotlin 扩展与作用域函数（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 扩展函数 ≈ Swift extension，但**本质是静态工具函数**（按声明类型分发、不能重写、可以在 null 上调用）。五大作用域函数 `let/run/with/apply/also` 是 Swift 没有的语法密度炸弹——初读 Kotlin 代码最大的障碍，本文给出一张决策表终结选择困难。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证。

---

## 1. 扩展函数：语法糖版的工具函数

```kotlin
// 给 String "添加"方法，this 指代接收者（可省略）
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) this else take(maxLength) + "..."

"Hello, World!".truncate(5)   // "Hello..."
"Hi".truncate(5)              // "Hi"

// 扩展属性（只能是计算属性，不能有存储）
val String.lastChar: Char
    get() = this[length - 1]

"Kotlin".lastChar             // 'n'
```

| 能力 | Kotlin 扩展 | Swift extension |
|---|---|---|
| 添加方法/计算属性 | ✅ | ✅ |
| 添加存储属性 | ❌ | ❌ |
| 声明位置 | **顶层函数**（也可在类内） | extension 块 |
| 实现协议/接口 | ❌（不能让已有类实现新接口） | ✅（retroactive conformance） |
| 访问 private 成员 | ❌ | 同模块内 ✅ |
| 组织代码分节 | 不常用 | 常用（// MARK: 配 extension） |

两个 Swift 做得到而 Kotlin 做不到的：**扩展不能给已有类补协议实现**（Kotlin 用包装类或接口委托绕），**扩展不能访问 private 成员**（它真的只是个外部函数）。

---

## 2. 扩展函数是静态分发的（必知的坑）

```kotlin
open class Shape
class Circle : Shape()

fun Shape.describe() = "形状"
fun Circle.describe() = "圆形"

val shape: Shape = Circle()
shape.describe()    // "形状" ！！按【声明类型】Shape 分发，不看运行时类型
```

扩展函数编译后就是个静态函数 `describe(receiver: Shape)`，**没有多态**。Swift extension 里的方法（非协议要求的）同样是静态分发，所以这个坑 iOS 开发者其实似曾相识——需要多态行为就用类的成员方法/接口方法，不要用扩展。

**成员函数永远优先于同签名的扩展函数**——给系统类写扩展时如果"不生效"，先检查是不是撞了成员方法名。

---

## 3. 可空接收者扩展：能在 null 上调用的方法

```kotlin
// 接收者类型写成 String?，函数体内处理 null
fun String?.orPlaceholder(): String = this ?: "(空)"

val name: String? = null
name.orPlaceholder()    // "(空)" —— 注意：不需要 ?. ，null 上直接调用！
```

Swift 里 `nil.someMethod()` 不可想象，Kotlin 这是官方惯用法——标准库的 `isNullOrEmpty()`、`isNullOrBlank()`、`orEmpty()` 都是这么实现的。**看到不带 `?.` 却在可空变量上调用的方法，就是可空接收者扩展。**

---

## 4. 五大作用域函数：一张表终结选择困难

它们本质都是 inline 高阶函数，区别只有两个维度：**上下文对象叫 `it` 还是 `this`**、**返回 lambda 结果还是对象本身**。

| 函数 | 对象引用 | 返回值 | 典型用途 |
|---|---|---|---|
| `let` | `it` | lambda 结果 | 空判断 + 转换：`x?.let { ... }` |
| `run` | `this` | lambda 结果 | 配置对象并计算出一个结果 |
| `with(x)` | `this` | lambda 结果 | 对同一对象连续操作（非扩展形式） |
| `apply` | `this` | **对象本身** | **对象配置**（builder 风格） |
| `also` | `it` | **对象本身** | 链中插入副作用（打日志、校验） |

```kotlin
// let：转换出新值，最常配合 ?. 做空判断
val result = nullableString?.let { "处理: $it" } ?: "无数据"

// apply：配置完返回对象自己 —— iOS 里 "let label = UILabel(); label.text=...; " 的终结者
val list = mutableListOf<Int>().apply {
    add(1)
    add(2)
}   // 返回 list 本身

// also：不打断链条地插一脚
val value = loadData().also { log("加载了: $it") }

// with：对一个对象做一串操作，收个结果
val text = with(StringBuilder()) {
    append("a")
    append("b")
    toString()
}

// run：apply 的"要结果"版
val length = "hello".run { length }
```

**记忆口诀**：

- 要把对象**变成别的东西** → `let`（可空转换）/ `run`（需要 this 语境）
- 要把对象**配置完还给我** → `apply`（配置）/ `also`（副作用）
- **90% 的场景只需要 `?.let` 和 `apply`**。团队代码评审常见病是作用域函数套娃（三层 `let` 嵌套），嵌套超过一层就该改成提前返回或局部变量——可读性 > 炫技。

Swift 对照：Swift 没有这组函数，最接近的是社区常见的 `then` 库（`UILabel().then { $0.text = ... }`），对应 Kotlin 的 `apply`。

---

## 5. infix 函数：自定义"中缀操作符"

```kotlin
infix fun Int.pow(exponent: Int): Int {
    var result = 1
    repeat(exponent) { result *= this }
    return result
}

2 pow 3      // 8 —— 省略点和括号
2.pow(3)     // 普通调用也行
```

条件：成员或扩展函数、单参数、标记 `infix`。标准库里天天见的 infix：

```kotlin
val pair = "key" to "value"     // to 就是 infix 函数，生成 Pair
for (i in 0 until 10) { }       // until
for (i in 10 downTo 1) { }      // downTo
```

`mapOf("a" to 1)` 里的 `to` 不是语法，是函数——理解这点后很多"魔法语法"就祛魅了。

---

## 6. 操作符重载：约定优于配置

Swift 可以自定义任意操作符（`<*>`、`|>`...），Kotlin 收紧为**只能重载固定集合的操作符**，通过 `operator fun` + 约定的函数名：

```kotlin
data class Vector(val x: Int, val y: Int) {
    operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
    operator fun times(scale: Int) = Vector(x * scale, y * scale)
}

Vector(1, 2) + Vector(3, 4)   // Vector(4, 6)
Vector(1, 2) * 2              // Vector(2, 4)
```

| 操作符 | 函数名 | 说明 |
|---|---|---|
| `+` / `-` / `*` / `/` | `plus`/`minus`/`times`/`div` | 算术 |
| `a[i]` / `a[i] = x` | `get`/`set` | 下标，对应 Swift subscript |
| `a in b` | `contains` | 包含判断 |
| `a()` | `invoke` | 对象当函数调 |
| `a..b` | `rangeTo` | 区间 |
| `==` | `equals` | 自动来自 data class |

`invoke` 值得单独一提——对象可以像函数一样调用：

```kotlin
class Greeter(private val greeting: String) {
    operator fun invoke(name: String): String = "$greeting, $name!"
}

val hello = Greeter("你好")
hello("世界")    // "你好, 世界!" —— 类似 Swift 的 callAsFunction
```

依赖注入框架和"用例类"（`GetUserUseCase`）常用 `invoke`，让 `getUser(id)` 这种调用背后其实是个对象。

---

## 7. 标准库即扩展：读源码的钥匙

Kotlin 标准库的"集合方法"绝大多数不是成员函数，而是扩展函数：

```kotlin
"hello".reversed()                        // 扩展
"1,2,3".split(",").map { it.toInt() }     // split 是扩展，map 也是扩展
```

意义有二：一是 cmd+点击进去看到的都是简洁的顶层函数，源码可读性很高，**遇到不懂的 API 直接点进去看是最快的学习方式**；二是团队可以用同样的方式给自己的类型建立"标准库"（`fun Date.toDisplayString()`、`fun Context.dp(value: Int)` 是 Android 项目的标配扩展）。

---

## 8. 速查小结：从 Swift 迁移的心智提醒

1. 扩展函数 ≈ Swift extension，但**不能补协议实现、不能访问 private、静态分发**——它就是个顶层工具函数加了语法糖。
2. **静态分发陷阱**：父类型变量调用扩展函数时，执行的是父类型版本，没有多态。
3. 可空接收者扩展让 `null.method()` 合法（`isNullOrEmpty()`），看到"没有 `?.` 的可空调用"别慌。
4. 作用域函数决策：**转换用 `let`，配置用 `apply`**，副作用用 `also`，其余场景再想 `run`/`with`；嵌套超一层就重构。
5. `to`、`until`、`downTo` 是 infix 函数不是语法，`mapOf("k" to v)` 就是普通函数调用。
6. 操作符重载只能用官方约定集（`plus`/`get`/`invoke`...），没有 Swift 自定义操作符的自由度（也少了被同事滥用的风险）。
7. 项目里给 `Context`、`View`、`String` 写公共扩展是 Android 惯例，相当于 iOS 项目里的 `UIView+Extensions.swift`。

---

*相关文档：作用域函数的 inline 原理见《Kotlin函数与Lambda_iOS视角.md》；`by lazy` 等属性委托见《Kotlin泛型与委托_iOS视角.md》。*
