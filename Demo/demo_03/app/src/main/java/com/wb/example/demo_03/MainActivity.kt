package com.wb.example.demo_03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wb.example.demo_03.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Compose 入口：用 setContent 代替传统的 setContentView(R.layout.xxx)
        setContent {
            AppNavigation()
        }
    }
}
