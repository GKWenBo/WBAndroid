package com.example.wblearncompose.demo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 通用详情页骨架：顶部带标题和返回箭头的 TopAppBar，下面是可滚动的内容区。
 *
 * 所有教学详情页都复用它，保证风格统一、减少重复样板。
 *
 * SwiftUI 对照：Scaffold ≈ 一个带 navigationBar 的容器；
 * TopAppBar ≈ .navigationTitle + .toolbar；onBack ≈ dismiss()。
 *
 * @param title 顶部标题
 * @param onBack 点击返回箭头时回调（通常是 navController.popBackStack）
 * @param content 页面内容，接收 Scaffold 给出的内边距（避开状态栏/AppBar）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

/**
 * 详情页里滚动列表的便捷封装：统一内边距和条目间距。
 *
 * 用 LazyColumn 而不是普通 Column + verticalScroll，是因为教学内容较长，
 * LazyColumn 只渲染可见项、性能更好（≈ SwiftUI 的 List / LazyVStack）。
 */
@Composable
fun DemoList(
    contentPadding: PaddingValues,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content,
    )
}

/**
 * 知识点小节：标题 + 一句说明 + 演示区。
 * 贯穿所有详情页，让每个 Compose API 都有清晰的“这是什么、怎么用”。
 */
@Composable
fun DemoSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
