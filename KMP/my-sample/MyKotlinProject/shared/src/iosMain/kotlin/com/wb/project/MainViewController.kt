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
