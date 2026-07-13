# Kotlin 类与对象（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 只有引用类型（class），**没有 Swift 的 struct/值类型**——`data class` 长得像 struct（自动 equals/copy），但赋值传递的是引用，这是心智模型上最大的坑。另外 Kotlin 类**默认 final**，`object` 关键字一行实现单例，`sealed class` 对应 Swift 的带关联值枚举。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证。

---

## 1. 主构造器与属性：声明即定义

```kotlin
// 主构造器直接写在类名后面，参数加 val/var 就自动成为属性
class Person(val name: String, var age: Int = 0) {

    // 计算属性：对应 Swift 的 computed property
    val isAdult: Boolean
        get() = age >= 18

    // 自定义 setter：field 是"幕后字段"，对应 Swift 里的存储本身
    var nickname: String = ""
        set(value) {
            field = value.trim()
        }

    // init 块：主构造器的补充逻辑（可以有多个，按顺序执行）
    init {
        require(name.isNotEmpty()) { "name 不能为空" }
    }
}

val p = Person("Alice", 17)
p.isAdult          // false
p.age = 18
p.isAdult          // true
p.nickname = "  Ali  "
p.nickname         // "Ali"（setter 里 trim 过）
```

| 概念 | Kotlin | Swift |
|---|---|---|
| 构造器参数即属性 | `class Person(val name: String)` | 需在 body 声明 + init 赋值（或 memberwise init） |
| 计算属性 | `val x get() = ...` | `var x: T { ... }` |
| 属性观察 | 自定义 setter / `Delegates.observable` | `willSet` / `didSet` |
| 幕后字段 | `field`（只能在 getter/setter 里用） | 隐式存储 |

**iOS 开发者注意**：Kotlin 没有 `willSet/didSet`，要么自定义 setter，要么用属性委托 `Delegates.observable`（见《Kotlin泛型与委托_iOS视角.md》）。

---

## 2. 次构造器（secondary constructor）

```kotlin
class Temperature(val celsius: Double) {
    // 必须委托给主构造器（this(...)），类似 Swift 的 convenience init
    constructor(fahrenheit: Int) : this((fahrenheit - 32) / 1.8)
}

Temperature(212).celsius   // 100.0
```

实践中次构造器很少用——**默认参数 + 命名参数 + companion object 工厂方法**几乎覆盖了所有场景。看到老代码里一堆 `constructor` 的，多半是从 Java 直译过来的。

---

## 3. data class：长得像 struct，但是引用类型

```kotlin
data class User(val name: String, val age: Int)

val u1 = User("Alice", 28)
val u2 = User("Alice", 28)

u1 == u2                    // true —— 自动生成 equals，按值比较（== 调用 equals）
u1.toString()               // "User(name=Alice, age=28)"

val u3 = u1.copy(age = 29)  // 拷贝并修改部分字段，对应 struct 的"改副本"习惯
val (name, age) = u3        // 解构：自动生成 component1/component2
```

`data class` 自动生成：`equals`/`hashCode`/`toString`/`copy`/`componentN`。对应 Swift 里 `struct` + `Equatable`/`Hashable` 自动合成。

**但它是引用类型！**（验证过的行为）：

```kotlin
data class MutableBox(var value: Int)

val a = MutableBox(1)
val b = a          // 不是拷贝！a、b 指向同一个对象
b.value = 99
println(a.value)   // 99 —— a 也"变"了。Swift struct 这里还是 1
```

**最佳实践：data class 的属性全部用 `val`（不可变）**。不可变 + `copy` 的用法就能获得接近 Swift struct 的安全性，这也是 Kotlin 社区的主流风格（尤其在多线程和 Compose 状态管理里几乎是强制的）。

**`==` vs `===`**：Kotlin 的 `==` 是 `equals`（值相等），`===` 才是引用相等。和 Swift 的 `==`/`===` 语义正好一致，但和 Java 的 `==` 完全相反——读 Java 代码时注意。

---

## 4. object：一行实现单例

```kotlin
object AppConfig {
    var debugMode = false
    fun describe(): String = "debug=$debugMode"
}

AppConfig.debugMode = true   // 直接用类名访问，全局唯一实例，线程安全的懒初始化
```

对应 Swift 的 `class AppConfig { static let shared = AppConfig(); private init() {} }` 五行样板，Kotlin 一个关键字解决。

`object` 还能用来创建匿名对象（对应 Java 匿名内部类 / Swift 临时遵循协议的场景）：

```kotlin
val listener = object : Clickable {
    override fun onClick() = "clicked"
}
```

---

## 5. companion object：Kotlin 的"static"

Kotlin 没有 `static` 关键字，类级别的成员放在 `companion object` 里：

```kotlin
class ApiClient private constructor(val baseUrl: String) {
    companion object {
        const val DEFAULT_TIMEOUT = 30   // 编译期常量，对应 static let

        // 工厂方法：配合 private 主构造器，对应 Swift 的静态工厂
        fun create(url: String): ApiClient = ApiClient(url.removeSuffix("/"))
    }
}

ApiClient.create("https://api.example.com/")  // 像调用静态方法一样
ApiClient.DEFAULT_TIMEOUT                     // 30
```

| Kotlin | Swift |
|---|---|
| `companion object` 里的成员 | `static` 成员 |
| `const val`（编译期常量，仅基本类型/String） | `static let` |
| 顶层 `val`/`fun`（文件级，不用包在类里） | 全局常量/函数 |

**Kotlin 特有习惯**：工具函数、常量经常直接写在**文件顶层**，不需要类包着。iOS 里的 `enum Constants { static let ... }` 模式在 Kotlin 里就是一个顶层 `val` 的事。

---

## 6. sealed class：对应 Swift 的带关联值枚举

这是 iOS 开发者会觉得最亲切的特性——**Swift 的 `enum` + associated values，在 Kotlin 里叫 `sealed class`**：

```kotlin
sealed class NetworkResult {
    data class Success(val data: String) : NetworkResult()
    data class Failure(val code: Int, val message: String) : NetworkResult()
    object Loading : NetworkResult()   // 无参数的 case 用 object
}

fun render(result: NetworkResult): String = when (result) {
    is NetworkResult.Success -> "成功: ${result.data}"    // 智能转换，直接拿关联值
    is NetworkResult.Failure -> "失败(${result.code}): ${result.message}"
    NetworkResult.Loading -> "加载中..."
    // 不需要 else！编译器知道所有子类，少写分支会编译报错 —— 同 Swift switch 穷举
}
```

| Swift | Kotlin |
|---|---|
| `enum Result { case success(String) }` | `sealed class` + `data class` 子类 |
| `switch` 穷举检查 | `when` 穷举检查（不写 `else` 才有检查！） |
| `case .success(let data)` | `is Success ->` 后智能转换 |
| 无关联值的 case | `object` 子类 |

**坑点**：`when` 写了 `else` 分支就**失去穷举检查**——以后新增子类编译器不会提醒你。处理 sealed 类型时尽量不写 `else`。

另有 `sealed interface`（子类可以同时实现多个）和 Kotlin 2.x 常用的嵌套写法，语义相同。UI 状态建模（`Loading/Content/Error`）、网络结果、导航事件——iOS 里用 enum 的地方，Kotlin 里就用 sealed class。

---

## 7. enum class：只适合"纯枚举"

```kotlin
enum class Direction(val degrees: Int) {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);   // 注意这个分号

    fun opposite(): Direction = entries[(ordinal + 2) % entries.size]
}

Direction.EAST.degrees        // 90
Direction.EAST.opposite()     // WEST
Direction.NORTH.name          // "NORTH"（对应 Swift rawValue 的味道）
Direction.entries             // 所有 case（Kotlin 1.9+，老代码是 values()）
```

**Kotlin 的 enum 每个 case 不能携带不同类型的关联值**——每个 case 只是同一个类的实例，构造参数结构必须一致。需要"不同 case 带不同数据"就用 sealed class。分工：**enum 管固定选项集，sealed class 管状态建模**。

---

## 8. 接口与默认实现

```kotlin
interface Clickable {
    fun onClick(): String                       // 抽象方法
    fun describe(): String = "可点击组件"        // 默认实现，对应 protocol extension
}

class Button : Clickable {
    override fun onClick() = "按钮被点击"
    // describe() 不实现就用默认的
}
```

和 Swift protocol + extension 提供默认实现的模式一致，但 Kotlin 的默认实现直接写在接口体内，且**接口方法的默认实现支持多态**（Swift protocol extension 的静态分发陷阱在 Kotlin 里不存在）。接口可以有属性声明（`val x: Int`），但不能有存储状态。

---

## 9. 继承：默认 final 是最大的文化差异

```kotlin
// 类默认 final！要被继承必须显式 open
abstract class Animal(val name: String) {
    abstract fun sound(): String                     // 抽象方法
    open fun intro(): String = "$name 说 ${sound()}" // open 才能被 override
}

class Dog(name: String) : Animal(name) {             // 继承和实现接口都用冒号
    override fun sound() = "汪"
    override fun intro(): String = super.intro() + "！"
}

val dog: Animal = Dog("旺财")
dog.intro()   // "旺财 说 汪！"
```

| 概念 | Kotlin | Swift |
|---|---|---|
| 默认可继承性 | **final**（须 `open`） | 默认可继承（可加 `final`） |
| 方法默认可重写性 | **final**（须 `open`） | 默认可重写 |
| 重写标记 | `override`（强制） | `override`（强制） |
| 继承语法 | `class Dog : Animal(name)` | `class Dog: Animal` |
| 可见性 | `public`(默认)/`internal`/`protected`/`private` | `open/public/internal/fileprivate/private` |

两个注意点：

1. **`internal` 是模块内可见**（一个 Gradle module），大型 Android 项目模块化后这是最常用的 API 隔离手段，对应 Swift 的 `internal`（Kotlin 默认是 `public`，Swift 默认是 `internal`——**默认值相反**！）。
2. 继承写法里父类带括号 `Animal(name)`——那是在调用父类构造器；实现接口不带括号。

---

## 10. 速查小结：从 Swift 迁移的心智提醒

1. **没有 struct**。`data class` 是引用类型，`b = a` 不拷贝——用全 `val` 属性 + `copy()` 找回值语义的安全感。
2. `==` 是 equals（值比较），`===` 才是引用比较——与 Swift 一致、与 Java 相反。
3. Swift 的 `enum` + 关联值 → Kotlin 的 `sealed class`；Kotlin 的 `enum class` 只用于固定选项集。
4. `when` 处理 sealed/enum 时**不要写 else**，保住编译器的穷举检查。
5. 单例用 `object`，"static 成员"用 `companion object`，工具函数直接写文件顶层。
6. 类和方法**默认 final**，设计基类时才加 `open`；Kotlin 社区偏好"组合优于继承"，继承树普遍很浅。
7. 默认可见性是 `public`（Swift 是 internal），跨模块 API 记得主动收紧成 `internal`。
8. 没有 `willSet/didSet`，用自定义 setter 或 `Delegates.observable`。

---

*相关文档：属性委托与类委托见《Kotlin泛型与委托_iOS视角.md》；`object : 接口` 匿名对象常见于回调，闭包用法见《Kotlin函数与Lambda_iOS视角.md》。*
