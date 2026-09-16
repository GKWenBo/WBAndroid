package com.example.wblearncompose.demo.screens.listdetail

/**
 * 一条示例文章。真实项目里这通常来自网络/数据库；这里用内存假数据演示。
 */
data class Article(
    val id: Int,
    val title: String,
    val author: String,
    val summary: String,
    val content: String,
)

/** 假数据源。 */
val sampleArticles = listOf(
    Article(
        id = 1,
        title = "Compose 是什么",
        author = "教学示例",
        summary = "声明式 UI 的基本思想与心智模型。",
        content = "Jetpack Compose 用 Kotlin 函数描述界面：界面是状态的函数。" +
            "状态变化时，Compose 自动重组受影响的部分并刷新，你不用手动 findViewById、" +
            "也不用手动同步数据到控件——这与 SwiftUI 的声明式思路完全一致。",
    ),
    Article(
        id = 2,
        title = "状态与重组",
        author = "教学示例",
        summary = "remember / mutableStateOf 与单向数据流。",
        content = "用 remember { mutableStateOf(...) } 声明会被观察的状态（≈ @State）。" +
            "把状态“提升”到共同的父级，子组件只接收数据和回调，这就是单向数据流，" +
            "让界面可预测、易测试。",
    ),
    Article(
        id = 3,
        title = "导航与列表-详情",
        author = "教学示例",
        summary = "NavHost 路由跳转与参数传递。",
        content = "navigation-compose 用 NavHost 登记路由，用 navController.navigate(route) 跳转，" +
            "详情页从 NavBackStackEntry 的 arguments 里取参数（如文章 id）。" +
            "这正是你现在看到的这个页面所演示的模式。",
    ),
)

/** 按 id 查找文章；找不到返回 null。 */
fun findArticle(id: Int): Article? = sampleArticles.firstOrNull { it.id == id }
