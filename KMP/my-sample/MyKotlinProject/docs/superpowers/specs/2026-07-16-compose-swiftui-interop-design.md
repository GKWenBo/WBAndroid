# Compose ↔ SwiftUI 互相集成示例 — 设计文档

日期：2026-07-16
项目：`KMP/my-sample/MyKotlinProject`（Compose Multiplatform + iosApp）

## 背景与目标

当前 iOS 侧演示了「Compose 内嵌一个 SwiftUI 视图」，但有两个问题：

1. `ContentView.swift` / `MainViewController.kt` 里存在注释掉的分支（`MyMainViewController()`），切换演示要改代码重编译，不能直接交互浏览。
2. 只有一个静态 SwiftUI 视图（一个 `VStack + Text`），没有真实的原生能力示例，也没有数据回传。

本次目标：

- **可交互浏览**：Compose 侧做一个示例列表，点击进入全屏 SwiftUI 详情，带返回按钮；不再靠注释代码切换。
- **新增三个 Compose→SwiftUI 原生示例**：SwiftUI Map、SwiftUI Web、SwiftUI 图片选择。
- **新增一个反向嵌套示例**：SwiftUI 页面内再嵌一个 Compose 视图，交互上直接看到双向集成。
- **双向通信**：至少「图片选择」把选中的图片回传给 Compose 显示；Map 回传选中坐标。
- **技术文档**：写一份「Compose ↔ SwiftUI 相互调用」文档，覆盖双向、原理、踩坑，iOS 开发者视角。
- 清理注释代码，让示例可读、可直接运行查看。

约束：iOS 部署目标 `18.2`（现代 `Map` iOS17+ / `PhotosPicker` iOS16+ 均可用；SwiftUI 原生 `WebView` 为 iOS26+，超过下限，Web 用 `WKWebView` 包一层）。

## 总体架构 —— Kotlin「原生视图工厂」桥接

问题根因：Swift 目前把**一个**写死的工厂闭包传进 Compose，切换示例只能改代码。解决方式是在 Kotlin 侧定义一个**桥接接口**，Swift 实现一次，Compose 通过它按需创建各个 SwiftUI `UIViewController`：

```
Kotlin (iosMain)                              Swift (iosApp)
────────────────                              ──────────────
interface NativeViewFactory   ◀──implements──   IOSNativeViewFactory
  createMapView(onPick)          ──────────────▶  UIHostingController(MapDemoView)
  createWebView(url)             ──────────────▶  UIHostingController(WebDemoView)
  createImagePicker(onPick)      ──────────────▶  UIHostingController(ImagePickerView)
  createComposeInSwiftUIView()   ──────────────▶  UIHostingController(ComposeInSwiftUIDemoView)
                                                     └─ 内部再用 UIViewControllerRepresentable
                                                        嵌 ComposeChildViewController()（Kotlin）
```

- Compose 掌握导航；每个示例只是「向工厂要一个 `UIViewController`」然后丢进 `UIKitViewController`。
- 新增示例 = 接口加一个方法 + Swift 加一个视图，导航与桥接不用动。
- 回传用 Kotlin 函数类型作为回调参数（`(Double,Double)->Unit`、`(ByteArray)->Unit`），Swift 视图在合适时机调用。

考虑过的其他方案：(a) 定义多个顶层工厂函数——数量一多签名散乱；(b) 枚举 + `when` 分发——回调不好类型化。单一注入接口是 KMP 惯用做法，回调类型安全，最终采用。

## 组件设计

### Kotlin — `shared/src/iosMain/kotlin/com/wb/project/`

**`SwiftUIInterop.kt`**
- `interface NativeViewFactory`：四个 `create*` 方法，返回 `UIViewController`；带回传的方法接收 Kotlin 回调。
  - `createMapView(onCoordinatePicked: (Double, Double) -> Unit): UIViewController`
  - `createWebView(urlString: String): UIViewController`
  - `createImagePicker(onImagePicked: (ByteArray) -> Unit): UIViewController`
  - `createComposeInSwiftUIView(): UIViewController`
- `data class DemoItem(val title: String, val subtitle: String, val kind: DemoKind)` + `enum class DemoKind { MAP, WEB, IMAGE_PICKER, COMPOSE_IN_SWIFTUI }`
- `val demoItems: List<DemoItem>`：四个示例的展示信息。

**`ComposeInterop.kt`**
- `fun ComposeChildViewController(): UIViewController = ComposeUIViewController { ... }`：给反向示例用的一个小 Compose 视图（供 Swift 端 `UIViewControllerRepresentable` 承载），演示 SwiftUI→Compose 方向。

**`SwiftUIDemoGallery.kt`**
- `@Composable fun SwiftUIDemoGallery(factory: NativeViewFactory)`：
  - 状态 `selectedDemo: DemoItem?`；为 `null` 显示 `LazyColumn` 列表，非 `null` 显示全屏详情。
  - 详情：顶部 Compose 工具条（标题 + 返回按钮，返回置 `selectedDemo = null`）+ `UIKitViewController(factory = { factory.create...() }, modifier = fillMaxSize())`。
  - 回传显示在 Compose 层：
    - Map：`onCoordinatePicked` 更新 `pickedCoordinate` 状态，工具条下方 `Text` 显示经纬度。
    - 图片选择：`onImagePicked(bytes)` → `org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()` → 详情下方 `Image` 显示（这是可见的双向证据）。
  - 处理 safe-area：根布局 `windowInsetsPadding(WindowInsets.systemBars)`；SwiftUI 全屏区域按需忽略/保留。

**`MainViewController.kt`**（重构）
- `fun MainViewController(factory: NativeViewFactory): UIViewController = ComposeUIViewController { SwiftUIDemoGallery(factory) }`
- 删除 `MyMainViewController()` 与旧的单工厂 `ComposeEntryPointWithUIViewController`、注释分支。

### Swift — `iosApp/iosApp/`

- **`IOSNativeViewFactory.swift`**：`class IOSNativeViewFactory: NativeViewFactory`，四个方法各返回 `UIHostingController(rootView:)`。
- **`MapDemoView.swift`**：MapKit `Map`，`MapReader` 把点击坐标转成经纬度并落一个标注，通过 `onCoordinatePicked` 回传。
- **`WebDemoView.swift`**：`WebView: UIViewRepresentable` 包 `WKWebView`，加载传入 URL；`WebDemoView` 提供一个默认 URL。
- **`ImagePickerDemoView.swift`**：`PhotosPicker` 选图 → `Data(PhotosPickerItem)` → `UIImage` → `pngData()` → `KotlinByteArray`，通过 `onImagePicked` 回传。
- **`ComposeInSwiftUIDemoView.swift`**：SwiftUI 视图，内部 `ComposeChildRepresentable: UIViewControllerRepresentable` 承载 `ComposeInteropKt.ComposeChildViewController()`，外面裹 SwiftUI 文案/背景，直观展示「SwiftUI 里嵌 Compose」。
- **`ContentView.swift`**（重构）：`ComposeView` 调 `MainViewControllerKt.MainViewController(factory: IOSNativeViewFactory())`；删除注释代码，保留 `#Preview`。

### 数据流（跨边界）

- 回调是 Kotlin 函数类型，Swift 侧当作 `@escaping` 闭包实现并在合适时机调用。
- 图片：Swift 侧转 PNG `Data → KotlinByteArray`；Compose 侧用 Skia 解码为 `ImageBitmap`。
- 坐标：`(Double, Double)`（lat, lng）直接过桥。

## 技术文档 —— `docs/compose-swiftui-interop.md`

面向 iOS 开发者，内容：

1. **两个方向的入口**
   - SwiftUI→Compose：App 根即是（`ComposeUIViewController` ← `UIViewControllerRepresentable`/`ComposeView`）。
   - Compose→SwiftUI：`UIKitViewController` 承载 `UIHostingController`。
2. **桥接原理**：`NativeViewFactory` 接口如何被 Swift 实现、Compose 如何调用；`UIHostingController` 与 `UIViewControllerRepresentable` 的角色对照（相当于 iOS 里 SwiftUI↔UIKit 的桥）。
3. **数据回传**：Kotlin 回调 → Swift 闭包；图片/坐标的跨语言转换（`Data`↔`ByteArray`、Skia 解码）。
4. **踩坑**：
   - safe-area / `WindowInsets`；`ignoresSafeArea` 与 Compose insets 的取舍。
   - `@OptIn(ExperimentalForeignApi::class)`（`UIKitViewController`）。
   - `isStatic = true` framework 导出、Kotlin 函数在 Swift 里的调用名（`XxxKt`）。
   - `UIKitViewController` 的 sizing / measurement 行为，`PhotosPicker` 无需 plist 权限。
5. **如何新增一个示例**：三步清单（接口方法 → Swift 视图 → 列表项）。

## 测试与验收

- 该功能是 iOS UI + 原生框架集成，主要靠**真机/模拟器手动验收**（列表→四个示例→返回→回传显示）。
- 共享逻辑无新增纯逻辑单元，故不新增 commonTest；如后续抽出坐标/字节转换的纯函数再补测。
- 验收清单：
  1. 启动进入 Compose 列表，四项可见。
  2. Map：点击地图落点，Compose 顶部显示坐标。
  3. Web：加载出网页。
  4. 图片选择：选图后 Compose 侧显示该图。
  5. 反向：进入后能看到 SwiftUI 包着的 Compose 子视图。
  6. 每个详情返回按钮回到列表；全程无需改代码。

## 非目标（YAGNI）

- 不引入 Compose Navigation 库（用简单状态导航）。
- 不做 Android 侧对应示例（这是 iOS 原生互操作专题）。
- 不做地图搜索/定位权限、Web 前进后退栈、图片多选等扩展。
