package com.wb.example.demo_05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wb.example.demo_05.ui.AppNavigation
import com.wb.example.demo_05.ui.theme.Demo_05Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Demo_05Theme {
                AppNavigation()
            }
        }
    }
}
