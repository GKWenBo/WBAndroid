package com.wb.example.demo_04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wb.example.demo_04.ui.AppNavigation
import com.wb.example.demo_04.ui.theme.Demo_04Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent 是 Compose 的声明式入口，对应 SwiftUI 的 WindowGroup { ... }
        setContent {
            Demo_04Theme {
                AppNavigation()
            }
        }
    }
}
