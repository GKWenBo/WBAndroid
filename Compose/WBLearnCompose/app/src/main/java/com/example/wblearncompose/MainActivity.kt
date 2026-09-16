package com.example.wblearncompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wblearncompose.demo.WBLearnComposeApp
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 应用入口。setContent 把 Compose 界面塞进这个 Activity（≈ SwiftUI App 的 WindowGroup）。
 * 用 WBLearnComposeTheme 包裹全局主题，再交给 WBLearnComposeApp 管理导航与页面。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 内容延伸到状态栏/导航栏下，做沉浸式
        setContent {
            WBLearnComposeTheme {
                WBLearnComposeApp()
            }
        }
    }
}
