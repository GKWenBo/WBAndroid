package com.wb.project

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

fun MainViewController() = ComposeUIViewController { App() }

fun MyMainViewController(): UIViewController = ComposeUIViewController {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("This is Compose code", fontSize = 20.sp)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun ComposeEntryPointWithUIViewController(createUIViewController: () -> UIViewController): UIViewController = ComposeUIViewController {
    Column (
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How to use SwiftUI inside Compose Multiplatform")
        UIKitViewController(
            factory = createUIViewController,
            modifier = Modifier.size(300.dp).border(2.dp, Color.Blue)
        )
    }
}