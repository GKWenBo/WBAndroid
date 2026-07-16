# Compose ↔ SwiftUI 互操作技术文档

面向已经熟悉 iOS/Swift、正在学 Kotlin Multiplatform 的读者。本文以 `MyKotlinProject` 里已经跑通的示例画廊为准，讲清楚 Compose Multiplatform 和 SwiftUI 是怎么互相"塞进对方"的——包括正向（SwiftUI 里嵌 Compose）和反向（Compose 里嵌 SwiftUI）两个方向。

## 1. 总览与两个方向的入口

先建立一个 iOS 开发者最熟悉的心智模型：**`UIViewController` 是双方共同的货币**。Compose 和 SwiftUI 各自都能把自己的整棵视图树包装成一个 `UIViewController`，之后怎么摆放、怎么嵌套，就跟摆两个普通的 `UIViewController`没有区别。

- Compose → `UIViewController`：Kotlin 侧的 `ComposeUIViewController { ... }`
- SwiftUI → `UIViewController`：Swift 侧的 `UIHostingController(rootView: ...)`

一句话对照：**`ComposeUIViewController` 把 Compose 变成 `UIViewController`；`UIHostingController` 把 SwiftUI 变成 `UIViewController`；剩下的事就是找一个 Representable/interop 包装器，把这个 `UIViewController` 塞进对方的视图树。**

### 方向一：SwiftUI → Compose（App 的根，正向）

这是整个 App 的启动路径。iOS 原生入口（`iOSApp`）加载 SwiftUI 的 `ContentView`，`ContentView` 用 `UIViewControllerRepresentable` 把 Compose 侧生成的 `UIViewController` 包起来：

```swift
// iosApp/iosApp/ContentView.swift
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(factory: IOSNativeViewFactory())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
```

对应的 Kotlin 侧入口很短，就是把整个示例画廊 Compose 函数包进 `ComposeUIViewController`：

```kotlin
// shared/src/iosMain/kotlin/com/wb/project/MainViewController.kt
fun MainViewController(factory: NativeViewFactory): UIViewController =
    ComposeUIViewController { SwiftUIDemoGallery(factory) }
```

如果你熟悉 UIKit，`UIViewControllerRepresentable` 的角色跟"把一个 UIKit VC 塞进 SwiftUI 树"完全一样，只不过这里塞进去的 VC 恰好是 Compose 渲染出来的。

### 方向二：Compose → SwiftUI（画廊详情页，反向）

在 Compose 侧的画廊详情页，用 `UIKitViewController(factory = { ... })` 承载一个由 Swift 返回的 `UIHostingController`：

```kotlin
// shared/src/iosMain/kotlin/com/wb/project/SwiftUIDemoGallery.kt（节选）
UIKitViewController(factory = nativeFactory, modifier = Modifier.fillMaxSize())
```

`nativeFactory` 是一个 `() -> UIViewController` 的 lambda，内部调用的是 Swift 实现的 `NativeViewFactory`（下一节详细讲）。`UIKitViewController` 是 Compose Multiplatform 提供的 API，作用等价于 SwiftUI 里的 `UIViewControllerRepresentable`——只不过方向反过来了：它是"把一个 UIKit/SwiftUI 的 VC 塞进 Compose 树"。

App 里的 `COMPOSE_IN_SWIFTUI` 示例把两个方向串在一起：外层是 Compose 画廊（方向一进来的），点进详情后用 `UIKitViewController` 加载一个 SwiftUI 页面（方向二），这个 SwiftUI 页面内部又用 `UIViewControllerRepresentable` 嵌了一个 Compose 子视图（`ComposeChildViewController`，方向一）。三层来回嵌套，验证互操作是可以任意深度组合的。

## 2. 桥接原理：`NativeViewFactory`

Compose 侧不知道任何 SwiftUI 的具体实现，只知道一个接口：

```kotlin
// shared/src/iosMain/kotlin/com/wb/project/SwiftUIInterop.kt
interface NativeViewFactory {
    /** 地图：点击地图回传选中坐标 (lat, lng) */
    fun createMapView(onCoordinatePicked: (Double, Double) -> Unit): UIViewController

    /** 网页：加载指定 URL（WKWebView） */
    fun createWebView(urlString: String): UIViewController

    /** 图片选择：选完把图片 PNG 字节回传给 Compose */
    fun createImagePicker(onImagePicked: (ByteArray) -> Unit): UIViewController

    /** 反向示例：SwiftUI 页面里再嵌一个 Compose 视图 */
    fun createComposeInSwiftUIView(): UIViewController
}
```

如果你写过 Swift `protocol` + 依赖注入，这跟"面向协议编程"是同一套思路：Kotlin（被调用方所在的模块之外的一侧，逻辑上更靠"核心"）只声明需要什么能力，具体实现留给外层。这里的"外层"就是 iOS App target 里的 Swift 代码：

```swift
// iosApp/iosApp/IOSNativeViewFactory.swift
class IOSNativeViewFactory: NativeViewFactory {
    func createMapView(onCoordinatePicked: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        UIHostingController(rootView: MapDemoView(onCoordinatePicked: onCoordinatePicked))
    }
    func createWebView(urlString: String) -> UIViewController {
        UIHostingController(rootView: WebDemoView(urlString: urlString))
    }
    func createImagePicker(onImagePicked: @escaping (KotlinByteArray) -> Void) -> UIViewController {
        UIHostingController(rootView: ImagePickerDemoView(onImagePicked: onImagePicked))
    }
    func createComposeInSwiftUIView() -> UIViewController {
        UIHostingController(rootView: ComposeInSwiftUIDemoView())
    }
}
```

四个方法都是同一个套路：接收 Compose 传来的参数/回调 → 包成 SwiftUI 视图 → 用 `UIHostingController` 转成 `UIViewController` 返回。`IOSNativeViewFactory` 的实例在 `ContentView.swift` 里创建一次，作为 `factory` 参数一路传进 `MainViewControllerKt.MainViewController(factory:)`，Compose 拿到这个接口后，只在用户点进某个详情页时才调用对应的 `createXxx`（见 `DemoDetail` 的 `remember(kind)`，下节细讲），不是启动时就全部创建好。

**Kotlin 顶层函数在 Swift 里的调用名规则**：Kotlin/Native 生成的 Objective-C header 会把一个文件里的顶层函数（不属于任何 class/object）收进一个以 `<文件名>Kt`命名的"伪类"里，作为它的静态方法。所以：

- `MainViewController.kt` 里的顶层函数 `fun MainViewController(...)` → Swift 里调用 `MainViewControllerKt.MainViewController(factory:)`
- `ComposeInterop.kt` 里的顶层函数 `fun ComposeChildViewController()` → Swift 里调用 `ComposeInteropKt.ComposeChildViewController()`（见 `ComposeInSwiftUIDemoView.swift` 里的 `ComposeChildRepresentable`）

这跟 Swift 里"文件不产生命名空间，函数名冲突要靠模块/类型限定"是不同的心智模型——Kotlin 编译到 ObjC/Swift 时，会强制把文件名塞进调用路径里，第一次遇到容易懵，记住"文件名 + Kt"这个规则就好。

## 3. 数据回传

### 回调类型的桥接

Kotlin 的函数类型编译到 Swift 后变成闭包类型，基础类型会被装箱成对应的 `Kotlin*` 包装类型：

| Kotlin（接口声明） | Swift（实现签名） |
|---|---|
| `(Double, Double) -> Unit` | `@escaping (KotlinDouble, KotlinDouble) -> Void` |
| `(ByteArray) -> Unit` | `@escaping (KotlinByteArray) -> Void` |

对 `Double` 这种值类型，Kotlin/Native 桥接层会把它装箱成 `KotlinDouble`，构造时要显式装箱：

```swift
// MapDemoView.swift
onCoordinatePicked(
    KotlinDouble(double: coord.latitude),
    KotlinDouble(double: coord.longitude)
)
```

Compose 侧收到的还是普通的 `Double`（Kotlin 内部会自动拆箱），所以 `DemoDetail` 里直接写 `factory.createMapView { lat, lng -> coordinate = lat to lng }`，`lat`/`lng` 就是 `Double`，不需要手动处理装箱。

### 图片回传：`UIImage` → `Data` → `KotlinByteArray` → `ImageBitmap`

图片选择示例（`ImagePickerDemoView.swift`）选完图后，先转成 PNG 的 `Data`，再手动逐字节拷贝成 `KotlinByteArray`：

```swift
extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
}
```

调用方式：

```swift
guard let png = uiImage.pngData() else { return }
onImagePicked(png.toKotlinByteArray())
```

Compose 侧拿到的是 Kotlin 原生的 `ByteArray`（桥接层会自动把 `KotlinByteArray` 转回 `ByteArray`），用 Skia 解码成 Compose 能画的 `ImageBitmap`：

```kotlin
// SwiftUIDemoGallery.kt
private fun decodeImage(bytes: ByteArray): ImageBitmap =
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
```

`org.jetbrains.skia.Image.makeFromEncoded` 直接吃编码后的 PNG/JPEG 字节流（不需要先解出宽高再拷贝像素），`toComposeImageBitmap()` 是 Compose Multiplatform 提供的扩展函数，把 Skia 的 `Image` 转成 Compose UI 的 `ImageBitmap`，然后就能像用 `UIImage` 一样直接塞进 `Image(image, ...)` 显示。

## 4. 踩坑清单

1. **`UIKitViewController` 需要 `@OptIn(ExperimentalForeignApi::class)`**。`SwiftUIDemoGallery.kt` 里 `DemoDetail` 函数上标了 `@OptIn(ExperimentalForeignApi::class)`——这是 Compose Multiplatform 目前还在实验阶段的 iOS 互操作 API，写的时候如果漏标会直接编译报错。

2. **`factory` lambda 建议自己缓存，别依赖 interop 内部实现**。`UIKitViewController(factory = ...)` 的 `factory` 参数本质上是"要不要重新创建原生 VC"的判断依据之一；`UIKitViewController` 内部虽然会对 factory 做一些 remember，但这属于 interop 实现细节，不宜依赖。为避免依赖 UIKitViewController 内部对 factory 的 remember 细节，稳妥做法是自己用 `remember(kind)` 缓存 factory lambda（回传用的 setter 是稳定的），这样重组时也不会重建原生视图。`DemoDetail` 用 `remember(kind)` 把 `nativeFactory` 缓存起来，只有 `kind` 变化（切换到另一个示例）才重新构造：

   ```kotlin
   val nativeFactory: () -> UIViewController = remember(kind) {
       when (kind) {
           DemoKind.MAP -> { { factory.createMapView { lat, lng -> coordinate = lat to lng } } }
           DemoKind.WEB -> { { factory.createWebView("https://www.jetbrains.com/lp/compose-multiplatform/") } }
           DemoKind.IMAGE_PICKER -> { { factory.createImagePicker { bytes -> pickedImage = decodeImage(bytes) } } }
           DemoKind.COMPOSE_IN_SWIFTUI -> { { factory.createComposeInSwiftUIView() } }
       }
   }
   ```

   注意 lambda 里捕获的 `coordinate`/`pickedImage` setter 本身是稳定的（`remember { mutableStateOf(...) }` 返回的 state 对象不变），所以回传数据本身不会导致 `nativeFactory` 重新计算，只有回传触发的重组会刷新上面 `coordinate?.let { ... }` 这些读取 state 的 UI，原生视图不受影响。

3. **Safe Area 要在两侧分别处理**。Compose 根布局用 `windowInsetsPadding(WindowInsets.systemBars)` 避开刘海/Home Indicator（`SwiftUIDemoGallery` 的最外层 `Box`）；SwiftUI 侧如果想要全屏铺满（比如地图、WebView），按需加 `.ignoresSafeArea()`（`ContentView` 的 `ComposeView()` 和 `WebDemoView` 的 `WebView(url:)` 都这么处理）。两边各管各的，互不知道对方的安全区处理逻辑，混用时容易出现"内容被系统栏遮住"或者"多留了一圈空白"的问题，需要肉眼核对。

4. **`PhotosPicker` 不需要相册权限 plist**。iOS 16+ 的 `PhotosPicker` 在独立进程里运行选择器 UI，App 进程拿不到完整相册访问权，因此不用像老的 `UIImagePickerController`/`PHPickerViewController` 那样在 `Info.plist` 里配 `NSPhotoLibraryUsageDescription`。

5. **`WKWebView` 走 https，注意 ATS**。示例里 `WebDemoView` 加载的是 `https://www.jetbrains.com/...`，如果换成 http 地址，会被 App Transport Security 拦截，需要在 `Info.plist` 里开洞（`NSAppTransportSecurity`），实际项目里应优先保证资源本身是 https。

6. **`KotlinByteArray` 逐字节拷贝对大图偏慢，这是教学取舍**。`Data.toKotlinByteArray()` 用 for 循环逐字节 `array.set(index:value:)`，对示例图片够用，但数据量大时（比如原图不压缩）会有明显开销。生产项目如果需要频繁传大字节流，通常会考虑用 `NSData`/内存映射等方式减少拷贝次数，这里为了让读者一眼看懂"字节怎么过桥"，选择了最直白但不是最快的写法。

7. **framework 要开 `isStatic = true`，`baseName = "Shared"`**。`shared/build.gradle.kts` 里配置：

   ```kotlin
   baseName = "Shared"
   isStatic = true
   ```

   对应到 Swift 侧就是 `import Shared`（`ContentView.swift`、`IOSNativeViewFactory.swift`、`MapDemoView.swift` 等文件顶部都能看到）。`isStatic = true` 生成静态库形式的 framework，避免额外的动态库加载和签名配置；`baseName` 决定了 Swift `import` 语句里的模块名，改名字要连着 Xcode 工程配置一起改。

## 5. 如何新增一个示例（三步清单）

以画廊里已有的四个示例为参照，新增一个示例只需要改三处：

1. **`NativeViewFactory` 加一个方法**（`shared/src/iosMain/kotlin/com/wb/project/SwiftUIInterop.kt`）：

   ```kotlin
   interface NativeViewFactory {
       // ...已有的四个
       fun createXxx(/* 需要的参数或回调 */): UIViewController
   }
   ```

2. **`IOSNativeViewFactory` 实现它**（`iosApp/iosApp/IOSNativeViewFactory.swift`），返回一个 `UIHostingController` 包着的新 SwiftUI 视图：

   ```swift
   func createXxx(/* 对应参数 */) -> UIViewController {
       UIHostingController(rootView: XxxDemoView(/* ... */))
   }
   ```

   新的 `XxxDemoView.swift` 按 `MapDemoView.swift`/`WebDemoView.swift` 的样子另起一个文件即可。

3. **注册进画廊**（`shared/src/iosMain/kotlin/com/wb/project/SwiftUIInterop.kt` 和 `SwiftUIDemoGallery.kt`）：
   - `DemoKind` 加一个枚举值；
   - `demoItems` 列表加一条 `DemoItem(title, subtitle, DemoKind.XXX)`；
   - `DemoDetail` 的 `when (kind) { ... }` 加一个分支，调用 `factory.createXxx(...)`。

以上三步做完，新示例就会自动出现在列表页，点进去即可交互，不需要改动 `SwiftUIDemoGallery` 之外的 Compose 代码。

## 6. 文件地图

| 文件 | 职责 |
|---|---|
| `shared/src/iosMain/kotlin/com/wb/project/SwiftUIInterop.kt` | 定义 `NativeViewFactory` 接口、`DemoKind` 枚举、`DemoItem` 数据类、`demoItems` 列表——Compose 侧对"需要哪些 SwiftUI 视图"的声明 |
| `shared/src/iosMain/kotlin/com/wb/project/SwiftUIDemoGallery.kt` | 画廊顶层 Composable：`SwiftUIDemoGallery`（列表/详情切换）、`DemoList`（列表页）、`DemoDetail`（详情页，内部用 `UIKitViewController` 承载 SwiftUI 视图）、`decodeImage`（PNG 字节转 `ImageBitmap`） |
| `shared/src/iosMain/kotlin/com/wb/project/MainViewController.kt` | App 入口：`MainViewController(factory:)`，把 `SwiftUIDemoGallery` 包成 `UIViewController` 供 Swift 调用 |
| `shared/src/iosMain/kotlin/com/wb/project/ComposeInterop.kt` | 反向示例用的独立小 Compose 视图：`ComposeChildViewController()`，被 SwiftUI 的 `ComposeChildRepresentable` 承载 |
| `iosApp/iosApp/ContentView.swift` | App 根视图：`ComposeView`（`UIViewControllerRepresentable`，调用 `MainViewControllerKt.MainViewController`）、`ContentView` |
| `iosApp/iosApp/IOSNativeViewFactory.swift` | `NativeViewFactory` 协议的 Swift 实现，四个 `createXxx` 方法分别返回四个 SwiftUI 视图的 `UIHostingController` |
| `iosApp/iosApp/MapDemoView.swift` | MapKit 地图示例，点击回传坐标（`KotlinDouble` 装箱） |
| `iosApp/iosApp/WebDemoView.swift` | `WKWebView` 加载网页示例（`WebView: UIViewRepresentable` 包 `WKWebView`） |
| `iosApp/iosApp/ImagePickerDemoView.swift` | `PhotosPicker` 选图示例，含 `Data.toKotlinByteArray()` 扩展 |
| `iosApp/iosApp/ComposeInSwiftUIDemoView.swift` | 反向嵌套示例：`ComposeChildRepresentable`（承载 Kotlin 侧的 `ComposeChildViewController`）+ `ComposeInSwiftUIDemoView`（外层 SwiftUI 页面） |
