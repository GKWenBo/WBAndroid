# Demo 03 · Compose UI（Week 2）

> 目标：用 Jetpack Compose 写声明式界面，掌握状态、`Modifier`、列表、导航与深色模式。
> 沿用 `demo_02` 的 gradle 骨架；本工程新增 Compose 编译器插件 + Compose BOM 依赖。

## 怎么运行
1. Android Studio 打开 `Demo/demo_03`。
2. 连接模拟器/真机，点 Run。
3. 看到登录页 → 点「登录」进入列表页 → 点右上角「退出」返回。登录页可切换深色模式，整页换肤。

## 本 Demo 涵盖
- [x] `setContent { }` 代替 `setContentView(R.layout.xxx)`（声明式入口）
- [x] `@Composable` 函数即 UI 组件（≈ SwiftUI 的 `View`）
- [x] `remember { mutableStateOf(...) }` 状态 + 自动重组（≈ `@State`）
- [x] `Modifier` 链式修饰（≈ SwiftUI 的 `modifier`）
- [x] `Column` / `Row` / `Box` 布局（≈ `VStack` / `HStack` / `ZStack`）
- [x] `OutlinedTextField` 受控输入（≈ `TextField`）
- [x] `LazyColumn` + `items`（≈ `List`，懒加载）
- [x] Compose Navigation：`NavHost` + `composable("route")`（≈ `NavigationStack`）
- [x] Material 3 深色模式切换（≈ `.preferredColorScheme`）
- [x] `Scaffold` + `TopAppBar`（≈ 带导航栏的容器）

## iOS 工程师易踩的坑
1. **重组不是重绘整个屏幕**：只有“读到了变化状态”的 Composable 会重组，性能靠这个保证；别在 Composable 里做耗时操作。
2. **`mutableStateOf` 必须包 `remember`**：否则每次重组都会重置成初始值（≈ `@State` 必须存在视图生命周期里）。
3. **`Modifier` 顺序有意义**：`padding` 在前/在后影响布局结果，和 SwiftUI 的 `padding().background()` 顺序同理。
4. **`LazyColumn` 不用 `for` 循环 + `Column`**：后者会把全部 item 一次性组合，长列表会卡。
5. **导航是“声明式路由表”**：`NavHost` 里 `composable("list")` 定义页面，`navController.navigate("list")` 跳转；返回用 `popBackStack`。没有 iOS 那种“push 一个 VC 实例”的概念。
6. **Compose 需要编译器插件**：本机在 `settings.gradle.kts` 统一声明 Kotlin 版本，模块只 `alias(libs.plugins.kotlin.compose)`；**不要**再写 `org.jetbrains.kotlin.android`（AGP 会自动应用，重复会报 `extension 'kotlin' already registered`，见 demo_02 教训）。

## 下一步
理解后可进入 [../demo_04_data_arch](../demo_04_data_arch)（MVVM + Retrofit 网络 + Room 本地缓存 + Koin 注入）。
