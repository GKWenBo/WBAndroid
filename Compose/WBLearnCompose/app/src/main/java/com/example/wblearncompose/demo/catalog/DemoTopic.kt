package com.example.wblearncompose.demo.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wblearncompose.demo.Destinations

/**
 * 一个教学主题（首页列表里的一行）。
 *
 * SwiftUI 对照：类似你为 List 定义的 Identifiable 数据模型，
 * 用 route 决定点击后跳转到哪个页面。
 */
data class DemoTopic(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

/**
 * 首页展示的全部主题。数据驱动 UI：改这里就能增减列表项，
 * 不用动 CatalogScreen 的渲染代码。
 */
val demoTopics = listOf(
    DemoTopic(
        title = "常用控件",
        subtitle = "Button / 输入框 / Switch / Slider / Checkbox …",
        icon = Icons.Filled.Widgets,
        route = Destinations.WIDGETS,
    ),
    DemoTopic(
        title = "布局",
        subtitle = "Column / Row / Box / weight / LazyGrid …",
        icon = Icons.Filled.Dashboard,
        route = Destinations.LAYOUT,
    ),
    DemoTopic(
        title = "UI 效果与 Material 组件",
        subtitle = "Card / 圆角阴影 / 渐变 / Dialog / Snackbar …",
        icon = Icons.Filled.AutoAwesome,
        route = Destinations.EFFECTS,
    ),
    DemoTopic(
        title = "动画",
        subtitle = "animate*AsState / AnimatedVisibility / 无限动画 …",
        icon = Icons.Filled.Animation,
        route = Destinations.ANIMATION,
    ),
    DemoTopic(
        title = "列表-详情实战",
        subtitle = "真实项目最常见的模式：列表点击 → 带参数跳详情",
        icon = Icons.AutoMirrored.Filled.ListAlt,
        route = Destinations.SAMPLE_LIST,
    ),
)
