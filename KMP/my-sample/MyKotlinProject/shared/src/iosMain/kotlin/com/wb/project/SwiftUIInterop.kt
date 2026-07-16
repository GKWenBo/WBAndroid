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
