# Kotlin 集合开发指南（面向 Swift/iOS 开发者）

> 核心思路：Kotlin 集合的设计哲学和 Swift 高度相似（都强调不可变性、函数式操作链），但**类型系统层面的差异**是最大的坑点——尤其是"不可变" `List` 到底是不是真的不可变。

---

## 1. 集合体系总览

Kotlin 标准库集合分为三大类，都在 `kotlin.collections` 包下：

| 类型 | Kotlin | Swift 对应 |
|---|---|---|
| 有序列表 | `List` / `MutableList` | `Array` / `[T]` |
| 无序不重复集合 | `Set` / `MutableSet` | `Set<T>` |
| 键值对 | `Map` / `MutableMap` | `Dictionary<K,V>` |

**关键差异点（也是最容易踩的坑）**：

Swift 里 `let arr: [Int]` 是真正的值类型，编译期保证不可变、写时复制（COW）。

Kotlin 的 `List`（只读接口）**不是不可变（immutable），而是只读（read-only）**。它只是隐藏了修改方法的接口，底层对象完全可能是一个 `MutableList`，如果有其他持有可变引用的地方修改了它，你手里的"只读" `List` 看到的数据也会跟着变。真正不可变的集合需要显式使用 `List.of()`（Java 9+ 互操作）或第三方不可变集合库（如 kotlinx.collections.immutable）。

```kotlin
val mutable = mutableListOf(1, 2, 3)
val readOnlyView: List<Int> = mutable   // 只是接口收窄，不是拷贝
mutable.add(4)
println(readOnlyView)  // [1, 2, 3, 4] —— 会变！这不是 Swift 的值语义
```

这是从 Swift 转 Kotlin 时**最容易产生误解、也最容易引入 bug 的地方**，尤其在多线程/多模块共享数据时。

---

## 2. 创建集合

```kotlin
// 只读（read-only 接口）
val list = listOf(1, 2, 3)
val set = setOf("a", "b", "c")
val map = mapOf("key1" to "value1", "key2" to "value2")

// 可变
val mutList = mutableListOf(1, 2, 3)
val mutSet = mutableSetOf("a", "b")
val mutMap = mutableMapOf("k" to 1)

// 空集合（需要显式指定类型，否则推断不出来）
val empty = emptyList<Int>()

// 特定实现类型
val arrayList = arrayListOf(1, 2, 3)        // ArrayList
val linkedMap = linkedMapOf("a" to 1)       // 保持插入顺序（LinkedHashMap）
```

Swift 对比：`let arr = [1, 2, 3]` 本身就是不可变的（`let` 语义），Kotlin 需要靠 `listOf` vs `mutableListOf` 这两个**不同的函数**来区分，而不是靠 `val`/`var` 关键字（这点非常反直觉，容易搞混：`val list = mutableListOf(1,2,3)` 里 `val` 修饰的是引用不可重新赋值，`list` 本身内容依然可变）。

---

## 3. List 常用操作

```kotlin
val list = listOf(1, 2, 3, 4, 5)

list[0]                      // 索引访问，同 Swift
list.first()                 // 同 Swift .first!（但更安全，见下）
list.firstOrNull()           // 对应 Swift 的 .first（返回 Optional）
list.last()
list.getOrNull(10)           // 越界返回 null，不崩溃
list.getOrElse(10) { -1 }    // 越界返回默认值

list.contains(3)             // 同 Swift .contains
3 in list                    // 更 Kotlin 风格的写法

list.indexOf(3)
list.subList(1, 3)           // 类似 Swift 的 slice，注意是左闭右开

list.reversed()
list.sorted()                // 升序，返回新 List
list.sortedDescending()
list.sortedBy { it }         // 按指定 key 排序，同 Swift sorted(by:)
```

**空安全对比**：Kotlin 的 `first()` 在集合为空时会抛异常（类似 Swift 强制解包崩溃），一定要养成优先用 `firstOrNull()` 的习惯，这和 Swift 里优先用 `.first` 而不是 `.first!` 是一个思路。

---

## 4. 函数式操作链（这是 Kotlin 集合的精华，和 Swift 几乎一一对应）

| Kotlin | Swift | 作用 |
|---|---|---|
| `map { }` | `map { }` | 转换每个元素 |
| `filter { }` | `filter { }` | 过滤 |
| `filterNot { }` | — | 反向过滤 |
| `reduce { acc, e -> }` | `reduce(_:_:)` | 累加，无初始值 |
| `fold(init) { acc, e -> }` | `reduce(into:)` / `reduce(_:_:)` | 累加，有初始值 |
| `flatMap { }` | `flatMap { }` | 展开+转换 |
| `mapNotNull { }` | `compactMap { }` | 转换并去除 nil/null |
| `groupBy { }` | `Dictionary(grouping:by:)` | 分组 |
| `sortedBy { }` | `sorted(by:)` | 排序 |
| `any { }` | `contains(where:)` | 是否存在满足条件的元素 |
| `all { }` | `allSatisfy { }` | 是否全部满足 |
| `none { }` | — | 是否全不满足 |
| `count { }` | `count(where:)` | 满足条件的数量 |
| `sumOf { }` | `reduce(0, +)` / `map().reduce()` | 求和 |
| `associateBy { }` | 手动构造 Dictionary | 转成 Map，key 由 lambda 决定 |
| `distinct()` | 手动去重 | 去重 |
| `chunked(n)` | 手动实现 | 按大小分块 |
| `windowed(n)` | 手动实现 | 滑动窗口 |
| `zip(other)` | `zip(_:_:)` | 两个集合按下标配对 |
| `partition { }` | — | 按条件一分为二（Pair） |

示例：

```kotlin
data class User(val name: String, val age: Int, val city: String)

val users = listOf(
    User("Alice", 28, "Beijing"),
    User("Bob", 35, "Shanghai"),
    User("Carol", 22, "Beijing")
)

// 链式操作，和 Swift 写法几乎一模一样
val result = users
    .filter { it.age > 20 }
    .sortedBy { it.age }
    .map { it.name }
// ["Carol", "Alice", "Bob"]

// 分组：Kotlin 的 groupBy 比 Swift 更简洁
val byCity = users.groupBy { it.city }
// {"Beijing": [Alice, Carol], "Shanghai": [Bob]}

// fold 类似 Swift reduce(into:)
val totalAge = users.fold(0) { acc, user -> acc + user.age }

// sumOf 是更直接的写法
val totalAge2 = users.sumOf { it.age }

// partition 一次拆成两个 List，Swift 没有直接等价物
val (adults, others) = users.partition { it.age >= 25 }
```

---

## 5. Sequence：Kotlin 特有的"惰性求值"概念（Swift 没有直接对应物）

这是 Kotlin 集合体系里 **Swift 开发者最容易忽略、但性能相关的重要概念**。

`List` 上的链式操作是**及早求值（eager）**：每一步 `map`/`filter` 都会立刻遍历整个集合、生成一个新的中间集合。链条越长，中间对象越多，性能开销越大。

`Sequence` 类似 Swift 的 `LazySequence`（`.lazy` 修饰符），是**惰性求值**：所有中间操作只是"记录步骤"，真正的遍历只发生一次，在调用 `toList()` / `first()` / `sum()` 等终止操作时才触发。

```kotlin
// eager：每步都会产生完整的中间 List
val eager = (1..1_000_000)
    .map { it * 2 }
    .filter { it % 3 == 0 }
    .take(5)

// lazy：几乎不产生中间集合，遇到 take(5) 满足后立刻停止遍历
val lazy = (1..1_000_000).asSequence()
    .map { it * 2 }
    .filter { it % 3 == 0 }
    .take(5)
    .toList()
```

经验法则：**数据量大（几千以上）或者链条长（3 步以上）时用 `.asSequence()`**；数据量小的时候用 `List` 链式调用就够了，没必要为了"性能"到处加 `asSequence()`，可读性优先。

---

## 6. Map 常用操作

```kotlin
val map = mapOf("a" to 1, "b" to 2, "c" to 3)

map["a"]                      // 返回 Int?（可能为 null），同 Swift Dictionary 下标
map.getOrDefault("z", 0)
map.getOrElse("z") { 0 }

map.keys                      // Set<String>
map.values                    // Collection<Int>
map.entries                   // Set<Map.Entry<String, Int>>

for ((k, v) in map) { }       // 解构遍历，同 Swift for (k, v) in dict

map.filterValues { it > 1 }
map.filterKeys { it != "a" }
map.mapValues { (_, v) -> v * 2 }

// 可变 Map
val mutMap = mutableMapOf("a" to 1)
mutMap["b"] = 2                          // 同 Swift 下标赋值
mutMap.getOrPut("c") { 100 }             // key 不存在则计算并插入，很常用
mutMap.remove("a")
```

---

## 7. 可变 vs 只读：修改操作

```kotlin
val mutList = mutableListOf(1, 2, 3)

mutList.add(4)
mutList.addAll(listOf(5, 6))
mutList.remove(1)              // 按值删除
mutList.removeAt(0)            // 按下标删除
mutList.removeIf { it > 4 }    // 同 Swift removeAll(where:)
mutList.clear()

// 原地修改所有元素
mutList.replaceAll { it * 2 }

// 只读集合"修改"实际上是生成新集合（+ / - 操作符重载）
val original = listOf(1, 2, 3)
val added = original + 4          // [1, 2, 3, 4]，original 不变
val removed = original - 2        // [1, 3]
```

`+`/`-` 操作符对只读集合生效，这一点和 Swift 的 `Array` 拼接语法（`arr + [4]`）体验一致，但背后语义不同：Kotlin 这里每次都会创建全新的底层数组，注意循环里频繁使用 `+` 的性能问题（应该用 `MutableList` + `add`）。

---

## 8. 解构声明（Destructuring）

```kotlin
val pair = Pair("Alice", 28)
val (name, age) = pair

// data class 天然支持解构（自动生成 component1/component2...）
data class Point(val x: Int, val y: Int)
val (x, y) = Point(1, 2)

// 遍历 List 的 index + value
for ((index, value) in list.withIndex()) {
    println("$index: $value")
}
```

Swift 没有完全等价的语法糖（Swift 的 tuple 解构接近，但 `data class` 自动生成 component 函数是 Kotlin 特有能力）。

---

## 9. Array（区别于 List，容易混淆）

Kotlin 里 `Array<T>` 是单独的类型，对应 Java 数组，**不是** `List` 的别名（这点和 Swift 的 `Array` 概念不同，Swift 只有一种"数组"）。

```kotlin
val arr = arrayOf(1, 2, 3)          // Array<Int>
val intArr = intArrayOf(1, 2, 3)    // IntArray，无装箱，性能更好
```

**实践建议**：日常业务代码优先用 `List`/`MutableList`，`Array` 主要用于和 Java 互操作、`vararg` 参数、或者需要避免装箱开销的高性能场景（如 `IntArray`/`FloatArray` 处理大量数值）。

---

## 10. 速查小结：从 Swift 迁移的几个心智提醒

1. `listOf()` 的"只读"不等于 Swift `let` 数组的"不可变"——它只是接口层面隐藏了修改方法，底层数据仍可能被别处修改。真正需要不可变保证时要格外小心共享引用。
2. `val`/`var` 控制的是**变量引用**能不能重新赋值，不代表集合内容能不能变——这和 Swift `let`/`var` 直接决定值语义是否可变完全不同。
3. 链式函数式操作（`map`/`filter`/`fold`...）思路和 Swift 几乎一致，可以直接迁移经验。
4. 数据量大或链条长时考虑 `.asSequence()` 惰性求值，Swift 对应 `.lazy`。
5. `Array` 在 Kotlin 里是独立类型，不是 `List` 的同义词，日常业务代码优先用 `List`。

---

*建议下一步学习：Kotlin 协程（Coroutines）中的 `Flow`，它是"异步版的 Sequence"，理解了本文的 Sequence 惰性求值概念后会顺很多。*
