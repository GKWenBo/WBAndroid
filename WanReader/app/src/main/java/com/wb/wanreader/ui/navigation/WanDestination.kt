package com.wb.wanreader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

// 底部 Tab 的路由表：route 是导航系统里的"URL"，唯一标识一个页面。
// iOS 对照：≈ TabView 的 tag + NavigationPath 的目的地类型，集中定义避免散落魔法字符串。
enum class WanDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "首页", Icons.Filled.Home),
    SQUARE("square", "广场", Icons.Filled.Star),
    MINE("mine", "我的", Icons.Filled.Person)
}
