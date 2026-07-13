package com.wb.example.demo_03.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wb.example.demo_03.ui.theme.AppTheme

/**
 * 应用导航 + 深色模式开关。
 *
 * 关键点（iOS 工程师对照）：
 * - `rememberNavController()` ≈ 一个导航栈管理器（类似 iOS 的 NavigationStack / UINavigationController）。
 * - `NavHost` + `composable("route")` ≈ 声明式路由表（类似 SwiftUI 的 `NavigationStack(path:)` + `destination`）。
 * - `remember { mutableStateOf(false) }` 是 Compose 的“可变状态 + 重组”基础：
 *   状态变化 → 读它的 Composable 自动重组（≈ SwiftUI 的 @State）。
 */
@Composable
fun AppNavigation() {
    // 深色模式开关状态：放在导航层，登录页与列表页共享
    var darkTheme by remember { mutableStateOf(false) }

    AppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLogin = { navController.navigate("list") },
                        darkTheme = darkTheme,
                        onToggleDark = { darkTheme = !darkTheme }
                    )
                }
                composable("list") {
                    ListScreen(onLogout = { navController.popBackStack("login", false) })
                }
            }
        }
    }
}
