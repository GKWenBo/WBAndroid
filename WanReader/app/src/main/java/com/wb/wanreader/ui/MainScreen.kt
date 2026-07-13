package com.wb.wanreader.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wb.wanreader.ui.home.HomeScreen
import com.wb.wanreader.ui.mine.MineScreen
import com.wb.wanreader.ui.navigation.WanDestination
import com.wb.wanreader.ui.square.SquareScreen

// App 的 UI 根：Scaffold(底部导航条) + NavHost(页面容器)。
// iOS 对照：≈ TabView { NavigationStack { ... } }，但 Android 用"单 NavHost + 路由"而非三个独立栈。
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 订阅当前路由，用于底部条选中态（≈ TabView 的 selection 绑定）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                WanDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Tab 切换三件套（企业项目标准写法）：
                                // 1) 回退到起始页再切，避免 Tab 间叠出深栈
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 2) 连点同一 Tab 不重复入栈
                                launchSingleTop = true
                                // 3) 恢复该 Tab 上次的浏览状态
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WanDestination.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(WanDestination.HOME.route) { HomeScreen() }
            composable(WanDestination.SQUARE.route) { SquareScreen() }
            composable(WanDestination.MINE.route) { MineScreen() }
        }
    }
}
