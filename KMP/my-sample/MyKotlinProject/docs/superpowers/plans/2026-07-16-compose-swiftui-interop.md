# Compose ↔ SwiftUI 互相集成示例 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把当前「Compose 内嵌单个 SwiftUI 视图」的 demo 重构成一个可交互浏览的示例集：Compose 列表 → 全屏 SwiftUI 详情，含 Map / Web / 图片选择 / 反向嵌套四个示例，并带数据回传，另配一份互操作技术文档。

**Architecture:** Kotlin 侧定义 `NativeViewFactory` 桥接接口，Swift 实现一次并返回各 `UIHostingController`；Compose 掌握导航与回传显示，通过 `UIKitViewController` 承载原生视图。反向方向用 `ComposeChildViewController()` + Swift `UIViewControllerRepresentable` 演示 SwiftUI 内嵌 Compose。

**Tech Stack:** Kotlin Multiplatform 2.4.0，Compose Multiplatform 1.11.1，SwiftUI / MapKit / WebKit / PhotosUI，iOS 部署目标 18.2。

## Global Constraints

- iOS 部署目标 `18.2`；现代 `Map`(iOS17+) 与 `PhotosPicker`(iOS16+) 可用；Web 用 `WKWebView`（不用 iOS26+ 的 SwiftUI `WebView`）。
- 共享 framework `baseName = "Shared"`，`isStatic = true`；Kotlin 顶层函数在 Swift 里通过 `<文件名>Kt` 调用。
- Kotlin 包名 `com.wb.project`，iosMain 路径 `shared/src/iosMain/kotlin/com/wb/project/`。
- `UIKitViewController` 需 `@OptIn(ExperimentalForeignApi::class)`。
- 不引入 Compose Navigation 库；不做 Android 对应示例。
- 提交信息保持干净，不加 Co-Authored-By 署名行。
- 中文注释/文案，iOS 开发者视角。

---

### Task 1: Kotlin 桥接接口、示例模型、反向 Compose 子视图、Compose 画廊、入口重构

**Files:**
- Create: `shared/src/iosMain/kotlin/com/wb/project/SwiftUIInterop.kt`
- Create: `shared/src/iosMain/kotlin/com/wb/project/ComposeInterop.kt`
- Create: `shared/src/iosMain/kotlin/com/wb/project/SwiftUIDemoGallery.kt`
- Modify: `shared/src/iosMain/kotlin/com/wb/project/MainViewController.kt`

**Interfaces:**
- Produces（供 Swift/后续任务使用）：
  - `interface NativeViewFactory`：
    - `fun createMapView(onCoordinatePicked: (Double, Double) -> Unit): UIViewController`
    - `fun createWebView(urlString: String): UIViewController`
    - `fun createImagePicker(onImagePicked: (ByteArray) -> Unit): UIViewController`
    - `fun createComposeInSwiftUIView(): UIViewController`
  - `enum class DemoKind { MAP, WEB, IMAGE_PICKER, COMPOSE_IN_SWIFTUI }`
  - `data class DemoItem(val title: String, val subtitle: String, val kind: DemoKind)`
  - `val demoItems: List<DemoItem>`
  - `fun ComposeChildViewController(): UIViewController`（Swift 侧以 `ComposeInteropKt.ComposeChildViewController()` 调用）
  - `fun MainViewController(factory: NativeViewFactory): UIViewController`（Swift 侧 `MainViewControllerKt.MainViewController(factory:)`）

- [ ] **Step 1: 写桥接接口与示例模型 `SwiftUIInterop.kt`**

```kotlin
package com.wb.project

import platform.UIKit.UIViewController

/**
 * Compose → SwiftUI 的桥接接口。
 *
 * Kotlin 侧只声明「我要一个 UIViewController」，具体由 Swift 实现（见 iosApp/IOSNativeViewFactory.swift）。
 * 带回传的示例用 Kotlin 函数类型作为回调参数，Swift 视图在合适时机调用即可把数据送回 Compose。
 */
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

enum class DemoKind { MAP, WEB, IMAGE_PICKER, COMPOSE_IN_SWIFTUI }

data class DemoItem(
    val title: String,
    val subtitle: String,
    val kind: DemoKind,
)

val demoItems: List<DemoItem> = listOf(
    DemoItem("SwiftUI 地图", "MapKit Map，点击地图回传坐标到 Compose", DemoKind.MAP),
    DemoItem("SwiftUI 网页", "WKWebView 加载网页", DemoKind.WEB),
    DemoItem("SwiftUI 图片选择", "PhotosPicker 选图并回传给 Compose 显示", DemoKind.IMAGE_PICKER),
    DemoItem("SwiftUI 内嵌 Compose", "反向：SwiftUI 页面里再嵌一个 Compose 视图", DemoKind.COMPOSE_IN_SWIFTUI),
)
```

- [ ] **Step 2: 写反向 Compose 子视图 `ComposeInterop.kt`**

```kotlin
package com.wb.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * 供「反向示例」使用：一个独立的小 Compose 视图，被 Swift 的
 * UIViewControllerRepresentable 承载，演示 SwiftUI → Compose 方向。
 */
fun ComposeChildViewController(): UIViewController = ComposeUIViewController {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)),
        contentAlignment = Alignment.Center,
    ) {
        Text("这是 SwiftUI 里嵌入的 Compose 视图", color = Color.White, fontSize = 16.sp)
    }
}
```

- [ ] **Step 3: 写 Compose 画廊 `SwiftUIDemoGallery.kt`**

```kotlin
package com.wb.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.Image as SkiaImage
import platform.UIKit.UIViewController

/**
 * 顶层画廊：selectedKind 为 null 显示列表，否则显示全屏 SwiftUI 详情。
 * 这是「不用注释代码、直接交互浏览」的核心：点列表项进详情，点返回回列表。
 */
@Composable
fun SwiftUIDemoGallery(factory: NativeViewFactory) {
    var selectedKind by remember { mutableStateOf<DemoKind?>(null) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            val current = selectedKind
            if (current == null) {
                DemoList(onSelect = { selectedKind = it })
            } else {
                DemoDetail(kind = current, factory = factory, onBack = { selectedKind = null })
            }
        }
    }
}

@Composable
private fun DemoList(onSelect: (DemoKind) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Compose ↔ SwiftUI 示例", modifier = Modifier.padding(16.dp), fontSize = 22.sp)
        LazyColumn(Modifier.fillMaxSize()) {
            items(demoItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onSelect(item.kind) },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.title, fontSize = 18.sp)
                        Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun DemoDetail(kind: DemoKind, factory: NativeViewFactory, onBack: () -> Unit) {
    var coordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var pickedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val title = remember(kind) { demoItems.first { it.kind == kind }.title }

    // 关键：用 remember(kind) 缓存工厂 lambda，避免回传导致 DemoDetail 重组时
    // 重新创建原生视图（否则地图会因每次点击重建而丢状态）。回传用的 setter 是稳定的。
    val nativeFactory: () -> UIViewController = remember(kind) {
        when (kind) {
            DemoKind.MAP -> {
                { factory.createMapView { lat, lng -> coordinate = lat to lng } }
            }
            DemoKind.WEB -> {
                { factory.createWebView("https://www.jetbrains.com/lp/compose-multiplatform/") }
            }
            DemoKind.IMAGE_PICKER -> {
                { factory.createImagePicker { bytes -> pickedImage = decodeImage(bytes) } }
            }
            DemoKind.COMPOSE_IN_SWIFTUI -> {
                { factory.createComposeInSwiftUIView() }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) { Text("← 返回") }
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 18.sp)
        }

        coordinate?.let { (lat, lng) ->
            Text("回传坐标：lat=$lat, lng=$lng", Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }
        pickedImage?.let { image ->
            Image(image, contentDescription = "回传图片", modifier = Modifier.fillMaxWidth().height(200.dp))
        }

        UIKitViewController(factory = nativeFactory, modifier = Modifier.fillMaxSize())
    }
}

/** PNG/JPEG 字节 → Compose ImageBitmap（iOS 上走 Skia/skiko）。 */
private fun decodeImage(bytes: ByteArray): ImageBitmap =
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
```

注意：上面用到了 `androidx.compose.foundation.Image`，需补一行 import。把它加到 import 区：`import androidx.compose.foundation.Image`。

- [ ] **Step 4: 重构入口 `MainViewController.kt`（替换整个文件内容）**

```kotlin
package com.wb.project

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS 入口：Compose 承载整个示例画廊。
 * Swift 侧传入 NativeViewFactory 实现（IOSNativeViewFactory），
 * 由 Compose 在需要时创建各 SwiftUI 视图。
 */
fun MainViewController(factory: NativeViewFactory): UIViewController =
    ComposeUIViewController { SwiftUIDemoGallery(factory) }
```

- [ ] **Step 5: 编译 Kotlin 验证通过**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`（无编译错误；Swift 尚未接入不影响 Kotlin 编译）

- [ ] **Step 6: 提交**

```bash
git add shared/src/iosMain/kotlin/com/wb/project/
git commit -m "KMP my-sample：Kotlin 桥接接口 + Compose 示例画廊 + 入口重构"
```

---

### Task 2: Swift 工厂 + 入口重构（占位视图，先跑通交互外壳）

**Files:**
- Create: `iosApp/iosApp/IOSNativeViewFactory.swift`
- Modify: `iosApp/iosApp/ContentView.swift`

**Interfaces:**
- Consumes: Task 1 的 `NativeViewFactory` 协议、`MainViewControllerKt.MainViewController(factory:)`。
- Produces: `class IOSNativeViewFactory: NativeViewFactory`（四个方法先返回占位 `UIHostingController`，后续任务替换成真实视图）。

- [ ] **Step 1: 写 Swift 工厂（占位实现）`IOSNativeViewFactory.swift`**

```swift
import SwiftUI
import Shared

/// Kotlin NativeViewFactory 的 Swift 实现。
/// 每个方法把一个 SwiftUI 视图包进 UIHostingController 返回给 Compose。
/// 本步先用占位视图，后续任务逐个替换成真实的 Map / Web / 图片选择 / 反向嵌套。
class IOSNativeViewFactory: NativeViewFactory {
    func createMapView(onCoordinatePicked: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        UIHostingController(rootView: Text("Map 占位"))
    }

    func createWebView(urlString: String) -> UIViewController {
        UIHostingController(rootView: Text("Web 占位：\(urlString)"))
    }

    func createImagePicker(onImagePicked: @escaping (KotlinByteArray) -> Void) -> UIViewController {
        UIHostingController(rootView: Text("图片选择占位"))
    }

    func createComposeInSwiftUIView() -> UIViewController {
        UIHostingController(rootView: Text("反向嵌套占位"))
    }
}
```

- [ ] **Step 2: 重构 `ContentView.swift`（替换整个文件内容）**

```swift
import UIKit
import SwiftUI
import Shared

/// 用 Compose 承载整个示例画廊；把 Swift 的 NativeViewFactory 实现传进去。
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

#Preview {
    ContentView()
}
```

- [ ] **Step 3: 构建 iOS App 验证通过**

Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build 2>&1 | tail -5`
Expected: `** BUILD SUCCEEDED **`
（若 scheme 名不同，用 `xcodebuild -project iosApp/iosApp.xcodeproj -list` 查看后替换。）

- [ ] **Step 4: 模拟器手动验收**

在 Xcode 里选一个 iOS 18.2+ 模拟器运行；确认：进入即是 Compose 列表，四项可见；点任一项进入详情看到「XX 占位」，点「← 返回」回到列表。全程不改代码即可切换。

- [ ] **Step 5: 提交**

```bash
git add iosApp/iosApp/IOSNativeViewFactory.swift iosApp/iosApp/ContentView.swift
git commit -m "KMP my-sample：Swift NativeViewFactory + 入口重构（占位视图跑通交互外壳）"
```

---

### Task 3: SwiftUI 地图示例（Map + 坐标回传）

**Files:**
- Create: `iosApp/iosApp/MapDemoView.swift`
- Modify: `iosApp/iosApp/IOSNativeViewFactory.swift:createMapView`

**Interfaces:**
- Consumes: `createMapView(onCoordinatePicked:)` 回调类型 `(KotlinDouble, KotlinDouble) -> Void`。
- Produces: `struct MapDemoView`。

- [ ] **Step 1: 写 `MapDemoView.swift`**

```swift
import SwiftUI
import MapKit

/// MapKit 地图（iOS 17+ Map API）。点击地图落一个标注，并把坐标回传给 Compose。
struct MapDemoView: View {
    let onCoordinatePicked: (KotlinDouble, KotlinDouble) -> Void

    @State private var position: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 39.9042, longitude: 116.4074),
            span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        )
    )
    @State private var picked: CLLocationCoordinate2D?

    var body: some View {
        MapReader { proxy in
            Map(position: $position) {
                if let picked {
                    Marker("已选", coordinate: picked)
                }
            }
            .onTapGesture { screenPoint in
                if let coord = proxy.convert(screenPoint, from: .local) {
                    picked = coord
                    onCoordinatePicked(
                        KotlinDouble(double: coord.latitude),
                        KotlinDouble(double: coord.longitude)
                    )
                }
            }
        }
    }
}
```

需在文件顶部让 `KotlinDouble` 可见：加 `import Shared`。

- [ ] **Step 2: 工厂接入真实视图（替换 `createMapView` 方法体）**

在 `IOSNativeViewFactory.swift` 中把：
```swift
    func createMapView(onCoordinatePicked: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        UIHostingController(rootView: Text("Map 占位"))
    }
```
替换为：
```swift
    func createMapView(onCoordinatePicked: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        UIHostingController(rootView: MapDemoView(onCoordinatePicked: onCoordinatePicked))
    }
```

- [ ] **Step 3: 构建验证**

Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build 2>&1 | tail -5`
Expected: `** BUILD SUCCEEDED **`

- [ ] **Step 4: 模拟器手动验收**

进入「SwiftUI 地图」：看到北京附近地图；点击地图任意点，出现标注，Compose 顶部（返回按钮下方）显示「回传坐标：lat=… lng=…」；返回列表正常。

- [ ] **Step 5: 提交**

```bash
git add iosApp/iosApp/MapDemoView.swift iosApp/iosApp/IOSNativeViewFactory.swift
git commit -m "KMP my-sample：SwiftUI 地图示例（点击回传坐标到 Compose）"
```

---

### Task 4: SwiftUI 网页示例（WKWebView）

**Files:**
- Create: `iosApp/iosApp/WebDemoView.swift`
- Modify: `iosApp/iosApp/IOSNativeViewFactory.swift:createWebView`

**Interfaces:**
- Consumes: `createWebView(urlString:)`。
- Produces: `struct WebDemoView`、`struct WebView: UIViewRepresentable`。

- [ ] **Step 1: 写 `WebDemoView.swift`**

```swift
import SwiftUI
import WebKit

/// 用 UIViewRepresentable 把 UIKit 的 WKWebView 桥进 SwiftUI（SwiftUI 原生 WebView 需 iOS 26+）。
struct WebView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        WKWebView()
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        if webView.url != url {
            webView.load(URLRequest(url: url))
        }
    }
}

struct WebDemoView: View {
    let urlString: String

    var body: some View {
        if let url = URL(string: urlString) {
            WebView(url: url)
                .ignoresSafeArea()
        } else {
            Text("URL 无效：\(urlString)")
        }
    }
}
```

- [ ] **Step 2: 工厂接入（替换 `createWebView` 方法体）**

把：
```swift
    func createWebView(urlString: String) -> UIViewController {
        UIHostingController(rootView: Text("Web 占位：\(urlString)"))
    }
```
替换为：
```swift
    func createWebView(urlString: String) -> UIViewController {
        UIHostingController(rootView: WebDemoView(urlString: urlString))
    }
```

- [ ] **Step 3: 构建验证**

Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build 2>&1 | tail -5`
Expected: `** BUILD SUCCEEDED **`

- [ ] **Step 4: 模拟器手动验收**

进入「SwiftUI 网页」：加载出 JetBrains Compose Multiplatform 页面；返回列表正常。

- [ ] **Step 5: 提交**

```bash
git add iosApp/iosApp/WebDemoView.swift iosApp/iosApp/IOSNativeViewFactory.swift
git commit -m "KMP my-sample：SwiftUI 网页示例（WKWebView 加载 URL）"
```

---

### Task 5: SwiftUI 图片选择示例（PhotosPicker + 字节回传）

**Files:**
- Create: `iosApp/iosApp/ImagePickerDemoView.swift`
- Modify: `iosApp/iosApp/IOSNativeViewFactory.swift:createImagePicker`

**Interfaces:**
- Consumes: `createImagePicker(onImagePicked:)` 回调类型 `(KotlinByteArray) -> Void`；Compose 侧已用 `decodeImage` 显示。
- Produces: `struct ImagePickerDemoView`、`Data.toKotlinByteArray()` 扩展。

- [ ] **Step 1: 写 `ImagePickerDemoView.swift`**

```swift
import SwiftUI
import PhotosUI
import Shared

/// Data → KotlinByteArray 的桥接（逐字节拷贝，演示用；大图可换更快的方式）。
extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
}

/// PhotosPicker 选图（iOS 16+，进程外选择，无需相册权限描述）。
/// 选完把图片转 PNG 字节，通过回调回传给 Compose 显示。
struct ImagePickerDemoView: View {
    let onImagePicked: (KotlinByteArray) -> Void

    @State private var selection: PhotosPickerItem?
    @State private var previewImage: UIImage?

    var body: some View {
        VStack(spacing: 16) {
            PhotosPicker("选择照片", selection: $selection, matching: .images)
                .buttonStyle(.borderedProminent)

            if let previewImage {
                Image(uiImage: previewImage)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 200)
            } else {
                Text("尚未选择，选完会同时回传给 Compose 显示")
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .onChange(of: selection) { _, newItem in
            Task {
                guard let data = try? await newItem?.loadTransferable(type: Data.self),
                      let uiImage = UIImage(data: data),
                      let png = uiImage.pngData() else { return }
                previewImage = uiImage
                onImagePicked(png.toKotlinByteArray())
            }
        }
    }
}
```

- [ ] **Step 2: 工厂接入（替换 `createImagePicker` 方法体）**

把：
```swift
    func createImagePicker(onImagePicked: @escaping (KotlinByteArray) -> Void) -> UIViewController {
        UIHostingController(rootView: Text("图片选择占位"))
    }
```
替换为：
```swift
    func createImagePicker(onImagePicked: @escaping (KotlinByteArray) -> Void) -> UIViewController {
        UIHostingController(rootView: ImagePickerDemoView(onImagePicked: onImagePicked))
    }
```

- [ ] **Step 3: 构建验证**

Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build 2>&1 | tail -5`
Expected: `** BUILD SUCCEEDED **`

- [ ] **Step 4: 模拟器手动验收**

进入「SwiftUI 图片选择」→ 点「选择照片」→ 选一张模拟器自带图片：SwiftUI 内出现预览，同时 Compose 顶部（返回按钮下方）出现同一张回传图片（Skia 解码）。返回列表正常。

- [ ] **Step 5: 提交**

```bash
git add iosApp/iosApp/ImagePickerDemoView.swift iosApp/iosApp/IOSNativeViewFactory.swift
git commit -m "KMP my-sample：SwiftUI 图片选择示例（PhotosPicker 选图回传 Compose 显示）"
```

---

### Task 6: 反向嵌套示例（SwiftUI 里嵌 Compose）

**Files:**
- Create: `iosApp/iosApp/ComposeInSwiftUIDemoView.swift`
- Modify: `iosApp/iosApp/IOSNativeViewFactory.swift:createComposeInSwiftUIView`

**Interfaces:**
- Consumes: `ComposeInteropKt.ComposeChildViewController()`（Task 1）。
- Produces: `struct ComposeInSwiftUIDemoView`、`struct ComposeChildRepresentable: UIViewControllerRepresentable`。

- [ ] **Step 1: 写 `ComposeInSwiftUIDemoView.swift`**

```swift
import SwiftUI
import Shared

/// 把 Kotlin 侧的 Compose 视图（ComposeChildViewController）桥进 SwiftUI。
struct ComposeChildRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ComposeInteropKt.ComposeChildViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// 反向示例：最外层是 SwiftUI，中间嵌一个 Compose 子视图。
/// 配合外层的 Compose 画廊，形成 Compose → SwiftUI → Compose 的完整双向演示。
struct ComposeInSwiftUIDemoView: View {
    var body: some View {
        VStack(spacing: 0) {
            Text("这是 SwiftUI 外层")
                .font(.headline)
                .padding()

            ComposeChildRepresentable()
        }
    }
}
```

- [ ] **Step 2: 工厂接入（替换 `createComposeInSwiftUIView` 方法体）**

把：
```swift
    func createComposeInSwiftUIView() -> UIViewController {
        UIHostingController(rootView: Text("反向嵌套占位"))
    }
```
替换为：
```swift
    func createComposeInSwiftUIView() -> UIViewController {
        UIHostingController(rootView: ComposeInSwiftUIDemoView())
    }
```

- [ ] **Step 3: 构建验证**

Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build 2>&1 | tail -5`
Expected: `** BUILD SUCCEEDED **`

- [ ] **Step 4: 模拟器手动验收**

进入「SwiftUI 内嵌 Compose」：上方是 SwiftUI 文案「这是 SwiftUI 外层」，下方蓝色区域是 Compose 文案「这是 SwiftUI 里嵌入的 Compose 视图」。返回列表正常。至此四个示例全部可交互浏览。

- [ ] **Step 5: 提交**

```bash
git add iosApp/iosApp/ComposeInSwiftUIDemoView.swift iosApp/iosApp/IOSNativeViewFactory.swift
git commit -m "KMP my-sample：反向嵌套示例（SwiftUI 页面里再嵌 Compose 视图）"
```

---

### Task 7: 互操作技术文档

**Files:**
- Create: `docs/compose-swiftui-interop.md`

**Interfaces:**
- Consumes: 前六个任务落地的类型/函数名，用作文档中的真实代码引用。

- [ ] **Step 1: 写 `docs/compose-swiftui-interop.md`**

内容按下列结构写全（用真实文件路径与本项目里的实际符号名，代码片段直接引用已实现的类型）：

1. **总览与两个方向的入口**
   - SwiftUI → Compose：App 根 `iOSApp → ContentView → ComposeView(UIViewControllerRepresentable) → MainViewControllerKt.MainViewController(factory:)`，Kotlin 侧 `ComposeUIViewController { ... }`。
   - Compose → SwiftUI：`UIKitViewController(factory = { ... })` 承载 Swift 返回的 `UIHostingController`。
   - 一句话对照：`ComposeUIViewController` 把 Compose 变成 `UIViewController`；`UIHostingController` 把 SwiftUI 变成 `UIViewController`；两个 Representable/interop 负责互相塞。

2. **桥接原理：`NativeViewFactory`**
   - 贴 `shared/src/iosMain/.../SwiftUIInterop.kt` 的接口，说明 Swift `IOSNativeViewFactory` 如何实现协议、Compose 如何按需调用。
   - 说明 Kotlin 顶层函数在 Swift 的调用名规则（`<文件名>Kt.函数名`），如 `MainViewControllerKt`、`ComposeInteropKt`。

3. **数据回传**
   - Kotlin 回调 `(Double,Double)->Unit` / `(ByteArray)->Unit` ↔ Swift `@escaping (KotlinDouble,KotlinDouble)->Void` / `(KotlinByteArray)->Void`。
   - 图片：Swift `UIImage.pngData() → Data → KotlinByteArray`（贴 `Data.toKotlinByteArray()`），Compose `org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()`。
   - 坐标：`(Double, Double)` 直接过桥；用 `KotlinDouble(double:)` 装箱。

4. **踩坑清单**
   - `UIKitViewController` 需 `@OptIn(ExperimentalForeignApi::class)`。
   - 回传引起重组会重建原生视图：用 `remember(kind)` 缓存 factory lambda（贴 `DemoDetail` 里的处理）。
   - safe-area：根布局 `windowInsetsPadding(WindowInsets.systemBars)`；SwiftUI 全屏视图按需 `.ignoresSafeArea()`。
   - `PhotosPicker` 进程外运行，无需相册权限 plist；`WKWebView` 加载 http 需注意 ATS（示例用 https）。
   - `KotlinByteArray` 逐字节拷贝对大图偏慢，属演示取舍。
   - framework `isStatic = true` / `baseName = "Shared"`，Swift `import Shared`。

5. **如何新增一个示例（三步清单）**
   - ① `NativeViewFactory` 加一个 `createXxx(...)` 方法；② `IOSNativeViewFactory` 实现它返回 `UIHostingController(rootView: XxxDemoView())`；③ `demoItems` 加一项 + `DemoKind` 加枚举值 + `DemoDetail` 的 `when` 加分支。

6. **文件地图**：列出本次涉及的 Kotlin/Swift 文件及各自职责（对照本计划 File Structure）。

- [ ] **Step 2: 提交**

```bash
git add docs/compose-swiftui-interop.md
git commit -m "KMP my-sample：Compose↔SwiftUI 互操作技术文档"
```

---

## Self-Review 记录

- **Spec 覆盖**：可交互浏览→Task1 画廊 + Task2 外壳；Map→Task3；Web→Task4；图片选择+回传→Task5；反向嵌套→Task6；文档→Task7；清理注释代码→Task1(MainViewController 重构) + Task2(ContentView 重构)。全部覆盖。
- **占位符扫描**：无 TBD/TODO；每个代码步骤给出完整代码。
- **类型一致性**：`NativeViewFactory` 四个方法名（`createMapView`/`createWebView`/`createImagePicker`/`createComposeInSwiftUIView`）在 Kotlin 接口、Swift 实现、`DemoDetail` 的 `when` 三处一致；回调类型 Kotlin `(Double,Double)->Unit`/`(ByteArray)->Unit` 与 Swift `(KotlinDouble,KotlinDouble)->Void`/`(KotlinByteArray)->Void` 对应一致；`DemoKind` 四个枚举值贯穿使用。
- **备注**：iOS UI + 原生框架集成以构建 + 模拟器手动验收为主，无纯逻辑单元故不新增 commonTest（符合 spec 测试策略）。
