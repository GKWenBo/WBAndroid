# Kotlin 型变（Variance）：in / out 详解

## 1. 问题的起点

```kotlin
class Box<T>(val value: T)

val strBox: Box<String> = Box("hello")
val anyBox: Box<Any> = strBox  // ❌ 编译错误！即使 String 是 Any 的子类型
```

Kotlin 泛型**默认是不型变的（invariant）**：`Box<String>` 和 `Box<Any>` 之间没有任何子类型关系，哪怕 `String : Any`。

这在 Swift 里同样成立：
```swift
struct Box<T> { let value: T }
let strBox: Box<String> = Box(value: "hello")
let anyBox: Box<Any> = strBox  // ❌ 同样报错
```
所以第一个结论：**这不是 Kotlin 特有的坑，Swift 的值类型泛型也是不型变的**。不同点在于 Kotlin 提供了 `in`/`out` 关键字让你**主动声明**某个泛型该怎么变型，Swift 没有对应语法（但 Swift 的 `Array`、闭包类型天然协变/逆变，是语言内置的特例，不是你能自己声明的）。

---

## 2. out：协变（Covariant）—— "只生产,不消费"

```kotlin
interface Producer<out T> {
    fun produce(): T       // ✅ T 只能出现在返回值位置（out position）
    // fun consume(item: T) // ❌ 编译错误，T 不能作为参数
}
```

声明 `out T` 后：
```kotlin
val strProducer: Producer<String> = ...
val anyProducer: Producer<Any> = strProducer  // ✅ 合法！
```

**直觉**：如果一个类型只会"吐出"T（生产者），那么"能吐出 String 的东西"当然也"能吐出 Any"（因为 String 也是一种 Any）。这就是协变。

标准库例子：`List<out E>`（Kotlin 的 `List` 本身就是协变的只读接口），所以：
```kotlin
val strings: List<String> = listOf("a", "b")
val anys: List<Any> = strings  // ✅ 合法
```

**Swift 类比**：Swift 的函数**返回值类型是协变的**——
```swift
let f: () -> String = { "hi" }
let g: () -> Any = f   // ✅ 合法，返回值协变
```
这和 `out` 的道理完全一致，只是 Swift 是语言内置行为，Kotlin 让你能对自定义泛型类也声明这个能力。

---

## 3. in：逆变（Contravariant）—— "只消费,不生产"

```kotlin
interface Consumer<in T> {
    fun consume(item: T)   // ✅ T 只能出现在参数位置（in position）
    // fun produce(): T    // ❌ 编译错误
}
```

声明 `in T` 后，子类型关系是**反过来**的：
```kotlin
val anyConsumer: Consumer<Any> = ...
val strConsumer: Consumer<String> = anyConsumer  // ✅ 合法！方向反了
```

**直觉**：一个"能吃掉 Any 的机器"，当然也"能吃掉 String"（因为喂给它的 String 也是 Any）。所以 `Consumer<Any>` 是 `Consumer<String>` 的子类型——方向和 T 的子类型方向相反，所以叫"逆变"。

标准库例子：`Comparable<in T>`：
```kotlin
val anyComparator: Comparator<Any> = ...
val strComparator: Comparator<String> = anyComparator // 语义上类似
```

**Swift 类比**：Swift 函数**参数类型是逆变的**——
```swift
let f: (Any) -> Void = { _ in }
let g: (String) -> Void = f   // ✅ 合法，参数逆变
```
"能处理任何东西的函数"当然也能当作"能处理 String 的函数"用。这跟 `in T` 是一回事。

---

## 4. 一句话记忆法

| 关键字 | 位置 | 类比 | Swift 对应 |
|---|---|---|---|
| `out T` | 只出现在返回值/输出位置 | 生产者 Producer | 函数返回值类型（协变） |
| `in T` | 只出现在参数/输入位置 | 消费者 Consumer | 函数参数类型（逆变） |
| 不加 | T 既生产又消费 | 既读又写的容器（如 `MutableList`） | 默认（不型变） |

记忆技巧：**out 对应 "out position"（输出位置=返回值），in 对应 "in position"（输入位置=参数）**。这也是为什么 `MutableList<T>` 不能加 `out`——它既有 `get(): T`（out 位置）又有 `add(item: T)`（in 位置），二者冲突，只能保持不型变。

---

## 5. 使用点型变（Use-site Variance / 类型投影）

如果一个类设计时没有声明 `in`/`out`（比如 `Array<T>`，因为它既能读也能写），但你在某次具体使用中只想读或只想写，可以在**调用处**局部声明：

```kotlin
fun copyFrom(from: Array<out Any>, to: Array<Any>) {
    // from 在这里被当作只读（协变），即使 Array 本身不型变
}
```

这叫**类型投影（type projection）**，等价于 Java 的通配符：

| Kotlin | Java |
|---|---|
| `Array<out T>`（使用点） | `List<? extends T>` |
| `Array<in T>`（使用点） | `List<? super T>` |
| `class Foo<out T>`（声明点） | 无直接对应，Java 没有声明点型变 |

Java 互操作时你会经常在反编译/注解里看到这些通配符，理解了 in/out 就自然理解了它们。

---

## 6. 常见坑

1. **`out T` 的类里写了参数为 T 的函数会直接编译报错**，不是运行时问题——这是 Kotlin 的静态型变检查，比 Java 通配符更早发现问题。
2. `MutableList<T>` 不能协变，所以 `MutableList<String>` 不是 `MutableList<Any>` 的子类型——这是**类型安全**的，如果允许的话你可以往 `MutableList<Any>`（其实是 `MutableList<String>`）里塞一个 `Int`，运行时会炸。
3. Swift 开发者容易下意识以为"Kotlin 的 `List` 和 Swift 的 `Array` 差不多"——但 Swift `Array` 是**值类型**，赋值即拷贝，压根不存在这个型变问题的本质起因（别名共享）；Kotlin 的 `List`/`MutableList` 都是**引用类型**的接口，型变问题的本质是"防止通过父类型引用篡改子类型背后的真实对象"。

---

## 7. 自测题

1. 为什么 `Consumer<Any>` 可以赋值给 `Consumer<String>` 类型的变量，而不是反过来？
2. `out T` 修饰的接口里，为什么不能有 `fun update(item: T)` 这样的方法？
3. Swift 的哪个语言特性和 Kotlin 的 `out` 原理一致？哪个和 `in` 一致？
4. `Array<out Any>` 和 `class Producer<out T>` 的区别是什么（提示：声明点 vs 使用点）？
5. 为什么 `MutableList<T>` 不能声明为 `MutableList<out T>`？
