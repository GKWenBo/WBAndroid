package com.example.wblearncompose.demo.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 首页目录（列表）。本身就是「列表-详情」结构里的“列表”那一半：
 * 用 LazyColumn 渲染主题卡片，点一张卡片跳到对应详情页。
 *
 * SwiftUI 对照：LazyColumn ≈ List / LazyVStack；
 * items(...) ≈ ForEach；onTopicClick ≈ NavigationLink 的跳转动作。
 *
 * @param onTopicClick 点击某个主题时回调，把该主题的 route 交给上层去导航
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onTopicClick: (route: String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Compose 快速入门") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // items 是 LazyColumn 的扩展，遍历数据列表生成每一行
            items(demoTopics) { topic ->
                TopicCard(topic = topic, onClick = { onTopicClick(topic.route) })
            }
        }
    }
}

/** 单个主题卡片。抽成独立 Composable 便于复用与预览。 */
@Composable
private fun TopicCard(
    topic: DemoTopic,
    onClick: () -> Unit,
) {
    // Card 自带圆角、阴影和点击涟漪；onClick 让整张卡可点
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 圆形图标底：Surface 提供背景色，Box 让图标居中
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = topic.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // 中间标题+副标题，用 weight(1f) 占满剩余宽度，把箭头挤到最右
            Column(modifier = Modifier.weight(1f)) {
                Text(text = topic.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = topic.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    WBLearnComposeTheme {
        CatalogScreen(onTopicClick = {})
    }
}
