package com.wb.example.demo_03.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 示例数据模型（≈ Swift 的 struct）
data class Item(val id: Int, val title: String, val desc: String)

/**
 * 列表页：演示 LazyColumn（≈ SwiftUI 的 List）+ 顶栏 + 退出导航。
 *
 * iOS 对照：
 * - `LazyColumn` + `items(list)` ≈ SwiftUI 的 `List(items) { ... }`。
 * - `LazyColumn` 是“懒加载”的，只有可见项才会组合（≈ UITableView 的复用机制）。
 * - `TopAppBar` ≈ 导航栏大标题/小标题。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(onLogout: () -> Unit) {
    // remember 缓存列表，避免每次重组都重新生成
    val items = remember {
        List(30) { i -> Item(i, "条目 #$i", "这是第 $i 条示例数据，演示 LazyColumn 懒加载。") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("列表") },
                actions = {
                    TextButton(onClick = onLogout) { Text("退出") }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* 点击项：真实项目里可跳转详情 */ }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.desc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
