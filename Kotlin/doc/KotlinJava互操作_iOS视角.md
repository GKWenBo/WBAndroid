# Kotlin ↔ Java 互操作（面向 Swift/iOS 开发者）

> 核心思路：把它当成 **Swift ↔ Objective-C 混编**来理解，几乎每个概念都有对应物——平台类型 ≈ 未标注 nullability 的 ObjC API 桥出来的 IUO，`@JvmStatic` 系列注解 ≈ `@objc` 系列，SAM 转换 ≈ block 与闭包互转。差别在于 Kotlin/Java 共享 JVM 字节码，互操作比 Swift/ObjC 顺滑得多——但**空安全在边界处失守**这一点两边一样凶险。

> 本文所有主体示例均已在 LearnKotlin 工程的单元测试中实际运行验证（含 Java 侧调用 Kotlin 的真实 Java 测试类）。

---

## 0. 对照总表

| 场景 | Kotlin/Java | Swift/ObjC 对应 |
|---|---|---|
| 调用无空标注的老代码 | 平台类型 `String!` | 桥接出的 IUO `String!` |
| 暴露"静态方法" | `@JvmStatic` | `@objc static` |
| 暴露字段 | `@JvmField` | `@objc` 属性 |
| 默认参数生成重载 | `@JvmOverloads` | ObjC 看不到 Swift 默认参数（无解） |
| 改暴露名称 | `@JvmName` | `@objc(customName:)` |
| 单方法接口 ↔ lambda | SAM 转换 | block ↔ 闭包自动桥接 |
| 空安全注解 | `@Nullable`/`@NonNull` | `NS_ASSUME_NONNULL` 宏 |

---

## 1. Kotlin 调 Java：getter/setter 自动变属性

```java
// Java 类（老代码，无空安全标注）
public class JavaUser {
    public String getName() { ... }
    public String getNickname() { ... }      // 可能返回 null
    public void setNickname(String n) { ... }
    public boolean isActive() { ... }
    public static String staticGreeting() { ... }
}
```

```kotlin
val user = JavaUser("Alice")

user.name              // getName() 自动映射成属性访问
user.nickname = "Ali"  // setNickname() 映射成赋值
user.isActive          // isXxx() 保持 is 前缀
JavaUser.staticGreeting()   // 静态方法直接类名调用
```

和 Swift 调 ObjC 的体验类似（`[obj name]` → `obj.name`），映射规则：`getXxx()/setXxx()` → 属性 `xxx`，`isXxx()` → 属性 `isXxx`。

---

## 2. 平台类型：互操作的头号风险

Java 方法没有空安全标注时，Kotlin 侧看到的返回类型是 **`String!`（平台类型）**——注意它**只出现在 IDE/编译器提示里，源码中写不出来**。含义：编译器不知道可不可空，**放弃检查**：

```kotlin
val s = JavaUser.maybeNull()   // Java 返回了 null，s 的类型是 String!
s.length                       // 编译通过！运行时 NullPointerException（已验证）
```

这和 Swift 调未标注 `nullability` 的 ObjC API 拿到 IUO 一模一样——**编译器保护失效，崩溃转移到运行时**。

**对策（和 iOS 的经验相同）**：

```kotlin
// 1. 在边界处主动声明可空类型，立刻恢复编译器保护
val nick: String? = user.nickname
nick ?: "(无昵称)"                       // 之后就是正常的空安全世界

// 2. 自己维护的 Java 代码加注解（对应 NS_ASSUME_NONNULL_BEGIN）
//    @Nullable String getNickname() → Kotlin 侧变成 String?，不再裸奔
```

**团队铁律：Java 边界的返回值一律显式标注类型**，别让平台类型顺着类型推断扩散进业务代码。

---

## 3. Java 调 Kotlin：@Jvm 系列注解（对应 @objc 系列）

Kotlin 的 object、默认参数、顶层函数在 Java 里没有直接对应物，需要注解调整暴露形态。以下全部经真实 Java 测试类验证：

```kotlin
@file:JvmName("TextUtils")   // 顶层函数的宿主类名（默认是 文件名Kt）

// 顶层函数 → Java 里是 TextUtils.capitalizeWords(...)
fun capitalizeWords(text: String): String = ...

object StringHelper {
    @JvmStatic                        // 生成真静态方法
    fun shout(s: String) = s.uppercase() + "!"

    const val VERSION = "1.0"         // const → 真正的 static final 字段
}

class Rectangle @JvmOverloads constructor(val width: Int, val height: Int = width) {
    @JvmField                          // 直接暴露字段，Java 不用走 getArea()
    val area = width * height
}
```

```java
// Java 侧（真实测试代码）
StringHelper.shout("hi");            // "HI!" —— @JvmStatic 的效果
StringHelper.INSTANCE.shout("hi");   // 不加注解时的原始形态（丑）
StringHelper.VERSION;                // const 直接当常量用

new Rectangle(3);                    // @JvmOverloads 生成的单参构造器
new Rectangle(3, 4);
square.area;                         // @JvmField：字段直访

TextUtils.capitalizeWords("hello world");   // @file:JvmName 的效果
```

| 注解 | 解决什么 | 不加的后果 |
|---|---|---|
| `@JvmStatic` | object/companion 成员变真静态 | Java 要写 `.INSTANCE.` / `.Companion.` |
| `@JvmField` | 暴露裸字段 | Java 只能走 getter |
| `@JvmOverloads` | 默认参数生成多个重载 | Java 必须传全部参数 |
| `@file:JvmName` | 顶层函数宿主类名 | Java 看到 `XxxKt.foo()` |

**心智对照**：这组注解的存在意义 = `@objc`/`@objcMembers`——纯 Kotlin 项目一个都不用写，只有需要被 Java 消费的 API 才加。项目全是 Kotlin 就忘掉它们。

---

## 4. SAM 转换：Java 单方法接口 ↔ Kotlin lambda

对应 ObjC block ↔ Swift 闭包的自动桥接。Java 的**单抽象方法接口**（SAM，如 `Runnable`、`OnClickListener`）在 Kotlin 里可以直接用 lambda 实现（已验证）：

```kotlin
// Java: interface StringTransformer { String transform(String input); }

val upper = StringTransformer { it.uppercase() }    // lambda 直接实现接口
upper.transform("abc")                              // "ABC"

// 传给 Java 方法时直接尾随 lambda —— Android 里天天写的 setOnClickListener 就是这个
StringTransformer.apply("x y z") { it.replace(" ", "-") }   // "x-y-z"

val task = Runnable { doWork() }                    // JDK 接口同理
```

**注意**：SAM 转换只对 **Java 接口**自动生效；Kotlin 自己的接口要用 lambda 得声明成 `fun interface`：

```kotlin
fun interface Validator {
    fun validate(input: String): Boolean
}
val notEmpty = Validator { it.isNotEmpty() }
```

---

## 5. 其他边界须知（速览）

- **受检异常**：Kotlin 没有 checked exception。Kotlin 调 Java 不强制 try/catch（舒服）；Java 调可能抛异常的 Kotlin 函数时，需要 Kotlin 侧加 `@Throws(IOException::class)` 声明（对应 Swift `throws` 桥接成 `NSError**` 的角色）。
- **基本类型装箱**：`Int?` 在 JVM 上是装箱的 `Integer`，`Int` 是原生 `int`——高频路径上可空基本类型有性能代价（对应 ObjC `NSNumber` vs 标量的区别）。
- **集合映射**：Kotlin 的 `List` 在 Java 侧就是 `java.util.List`（零成本共享，没有 Swift/ObjC 那种桥接拷贝开销）；但 Java 侧拿到"只读" `List` 后调用 `add()` 会抛 `UnsupportedOperationException`——只读约束 Java 不认，运行时才炸。
- **data class 的 componentN**：Java 侧看不到解构，只有 `component1()` 这种奇怪方法名——data class 主要为 Kotlin 消费者设计。

---

## 6. 速查小结：从 Swift/ObjC 混编经验迁移

1. **整体心智**：Kotlin↔Java ≈ Swift↔ObjC，但共享字节码所以更顺滑；唯一凶险处与 iOS 相同——**空安全在边界失守**。
2. **平台类型 `String!` ≈ 桥接 IUO**：Java 返回值一律显式标 `String?`/`String`，把裸奔类型挡在边界。
3. 自家 Java 代码补 `@Nullable`/`@NonNull` ≈ 给 ObjC 头文件加 `NS_ASSUME_NONNULL`——一劳永逸的修法。
4. `@JvmStatic`/`@JvmField`/`@JvmOverloads`/`@JvmName` ≈ `@objc` 家族：只在 API 需要被 Java 消费时才加。
5. SAM 转换 ≈ block/闭包桥接：Java 单方法接口直接给 lambda；Kotlin 自己的接口要 `fun interface`。
6. Kotlin 只读 `List` 传给 Java 后修改会**运行时**抛异常——跨语言边界传集合优先传不可变快照（`toList()`）。

---

*相关文档：平台类型的空安全背景见《Kotlin空安全与类型系统_iOS视角.md》；lambda 与函数类型见《Kotlin函数与Lambda_iOS视角.md》。*
