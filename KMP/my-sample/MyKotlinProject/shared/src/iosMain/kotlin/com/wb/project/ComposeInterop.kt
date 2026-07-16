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
