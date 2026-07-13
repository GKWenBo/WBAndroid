# Kotlin 泛型与委托（面向 Swift/iOS 开发者）

> 核心思路：泛型部分——Kotlin 有 Swift 没有的**声明处型变（`in`/`out`）**和**类型擦除**（以及绕过它的 `reified`）。委托部分——`by` 关键字一鱼两吃：**属性委托**（`by lazy` 只是冰山一角，还能自制 `didSet`、UserDefaults 包装）和**类委托**（接口转发零样板，Swift 得手写 protocol 转发的场景一行搞定）。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证。

---

## 1. 泛型基础：和 Swift 几乎一致

```kotlin
// 泛型函数
fun <T> firstOrDefault(list: List<T>, default: T): T = list.firstOrNull() ?: default

// 泛型类 + 上界约束（对应 Swift 的 <T: Numeric> 或 where 子句）
class NumberBox<T : Number>(val value: T) {
    fun doubled(): Double = value.toDouble() * 2
}

NumberBox(3.14).doubled()   // 6.28
// NumberBox("str")         // 编译错误：String 不满足 T : Number
```

| 概念 | Kotlin | Swift |
|---|---|---|
| 泛型函数 | `fun <T> f(x: T)` | `func f<T>(_ x: T)` |
| 上界约束 | `<T : Number>` | `<T: Numeric>` |
| 多重约束 | `where T : A, T : B` | `where T: A & B` |
| 默认上界 | `Any?`（可空！） | 无约束 |

**注意默认上界是 `Any?`**：`fun <T> f(x: T)` 的 T 可以是可空类型。要求非空写 `<T : Any>`。

---

## 2. 型变：Kotlin 比 Swift 更显式

Swift 里泛型类型基本是不变的（invariant），只有标准库集合靠编译器魔法协变。Kotlin 把这件事交给开发者声明——**`out` 协变（只产出），`in` 逆变（只消费）**：

```kotlin
// out：T 只出现在返回值位置 → Producer<String> 可当 Producer<Any> 用
interface Producer<out T> {
    fun produce(): T
}
val producer: Producer<Any> = StringProducer()   // ✅ 协变（已验证）

// in：T 只出现在参数位置 → Consumer<Any> 可当 Consumer<String> 用
interface Consumer<in T> {
    fun consume(item: T): String
}
val consumer: Consumer<String> = AnyConsumer()   // ✅ 逆变（已验证）
```

记忆法：**out = 输出 = 协变（producer）；in = 输入 = 逆变（consumer）**。标准库的 `List<out E>` 是协变的，所以 `List<String>` 能赋给 `List<Any>`（`MutableList` 不行——它既进又出）：

```kotlin
val strings: List<String> = listOf("a", "b")
val objects: List<Any> = strings    // ✅ 编译通过，Swift 里 [String] as [Any] 的既视感
```

**星投影 `List<*>`**：不关心类型参数时用（对应 Swift 的 `any Collection` 的模糊感）——能读（读出来是 `Any?`）不能写：

```kotlin
val unknown: List<*> = listOf(1, "two", 3.0)
unknown.size            // ✅ 3
val x: Any? = unknown.first()   // 只能当 Any? 用
```

---

## 3. 类型擦除与 reified：Swift 开发者的新概念

Swift 泛型运行时保留类型信息（`T.self` 随便用）；**JVM 泛型是编译期擦除的**——运行时 `List<String>` 和 `List<Int>` 是同一个类，所以这些写法编译不过：

```kotlin
fun <T> isType(x: Any) = x is T          // ❌ Cannot check for instance of erased type
```

Kotlin 的解法：**`inline` + `reified`**——函数体内联到调用处后，T 在编译期已知，就"实化"了：

```kotlin
inline fun <reified T> typeName(): String = T::class.simpleName ?: "?"

typeName<String>()   // "String"
typeName<Int>()      // "Int"

inline fun <reified T> List<Any>.firstOfType(): T? {
    for (element in this) {
        if (element is T) return element   // ✅ reified 后可以 is 检查
    }
    return null
}

listOf(1, "two", 3.0).firstOfType<String>()   // "two"
```

**实际意义**：框架 API 里大量出现——`Gson().fromJson<User>(json)`、`intent.getParcelableExtra<User>()`、Koin 的 `get<UserRepository>()`，这些不用传 `User::class.java` 的舒服写法全靠 reified。标准库的 `filterIsInstance<T>()` 就是现成的 `firstOfType` 全量版。

---

## 4. 属性委托：`by` 把属性的读写逻辑外包出去

`val x by 委托对象` = 这个属性的 get/set 转发给委托对象。标准库自带三大件：

```kotlin
// ① lazy：首次访问才计算，只计算一次（已验证）
val expensive: String by lazy { loadFromDisk() }

// ② observable：Kotlin 版的 didSet（已验证）
var status: String by Delegates.observable("初始") { _, old, new ->
    changes.add("$old -> $new")
}
status = "在线"   // 回调收到 "初始 -> 在线"

// ③ Map 委托：属性值从 Map 里取（轻量配置/JSON 场景，已验证）
class Config(map: Map<String, Any?>) {
    val host: String by map
    val port: Int by map
}
Config(mapOf("host" to "api.example.com", "port" to 443)).port   // 443
```

| Kotlin | Swift 对应 |
|---|---|
| `by lazy` | `lazy var` |
| `Delegates.observable` | `didSet` |
| `Delegates.vetoable` | `willSet` + 可否决 |
| 自定义委托 | property wrapper（`@AppStorage` 等） |

**自定义委托 ≈ Swift 的 property wrapper**。实现 `ReadWriteProperty` 接口即可（已验证）：

```kotlin
class TrimmedString : ReadWriteProperty<Any?, String> {
    private var stored = ""
    override fun getValue(thisRef: Any?, property: KProperty<*>) = stored
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        stored = value.trim()
    }
}

class Profile {
    var bio: String by TrimmedString()
}
profile.bio = "  iOS 转 Android  "
profile.bio   // "iOS 转 Android"
```

对照 iOS：`@UserDefault("key") var name: String` 这种 property wrapper 惯用法，Android 里就是 `var name: String by SharedPreferenceDelegate("key")`——思路完全同构。Android 官方库里 `by viewModels()`、`by lazy`、Compose 的 `by remember { mutableStateOf() }` 全是这个机制。

---

## 5. 类委托：接口转发零样板

组合优于继承的痛点：包装一个对象实现某接口，得手写 N 个转发方法。Swift 里没有语言级方案（要么手写要么宏），**Kotlin 一个 `by` 解决**（已验证）：

```kotlin
interface Logger {
    fun log(message: String): String
}

class PrefixLogger(private val prefix: String) : Logger {
    override fun log(message: String) = "[$prefix] $message"
}

// Service 实现 Logger，所有方法自动转发给构造传入的 logger
class Service(logger: Logger) : Logger by logger {
    fun doWork(): String = log("工作完成")   // 直接用，像自己的方法
}

Service(PrefixLogger("APP")).doWork()   // "[APP] 工作完成"
```

需要定制某个方法时正常 `override` 即可，其余继续自动转发。典型用途：装饰器模式（给 Repository 加缓存层/日志层）、用组合替代继承扩展一个类的能力。

---

## 6. 速查小结：从 Swift 迁移的心智提醒

1. 泛型语法基本平移；注意**默认上界是 `Any?`**，要求非空写 `<T : Any>`。
2. `out`=只产出=协变，`in`=只消费=逆变——Swift 没有的显式型变声明，读框架源码（`Flow<out T>`、`Comparable<in T>`）需要认识它们。
3. **JVM 泛型运行时被擦除**——`x is T` 编译不过是常态，解法是 `inline fun <reified T>`；调 API 时看到不用传 `::class.java` 的都是 reified 的功劳。
4. `by lazy` ≈ `lazy var`，`Delegates.observable` ≈ `didSet`，自定义属性委托 ≈ property wrapper——iOS 的属性包装经验直接复用。
5. 类委托 `: 接口 by 对象` 是 Swift 没有的福利：装饰器/组合转发一行搞定，别再手写转发方法。
6. Android 代码里 `by viewModels()`、`by remember`、`by inject()` 满天飞——看到 `by` 就想"这个属性的读写被外包给谁了"。

---

*相关文档：`by lazy` 与 `lateinit` 的选择见《Kotlin空安全与类型系统_iOS视角.md》；inline 函数机制见《Kotlin函数与Lambda_iOS视角.md》。*
